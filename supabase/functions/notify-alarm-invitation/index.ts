// Supabase Edge Function: notify-alarm-invitation
//
// Database Webhook (INSERT on public.alarm_invitations) 으로 호출된다.
// 대상 사용자의 push_token 으로 FCM HTTP v1 data 메시지를 보낸다.
//
// 개인정보 원칙 (docs/DESTINATION_ALARM_PRIVACY.md):
// 이 함수는 초대 사실만 전달한다. 위치, 도착, 근접, 알람 발생, 확인 정보는
// 읽지도 보내지도 않는다. 페이로드는 type, invitation_id, requester_nickname, label 뿐이다.
//
// 필요한 환경 변수:
//   SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY  (Edge Function 런타임이 자동 제공)
//   FCM_SERVICE_ACCOUNT                      (Firebase 서비스 계정 JSON 문자열)

type WebhookPayload = {
  type: "INSERT" | "UPDATE" | "DELETE";
  table: string;
  schema: string;
  record: InvitationRecord | null;
  old_record: InvitationRecord | null;
};

type InvitationRecord = {
  id: string;
  requester_id: string;
  target_id: string;
  label: string | null;
  status: string;
};

type ServiceAccount = {
  client_email: string;
  private_key: string;
  project_id: string;
  token_uri?: string;
};

const FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
const DEFAULT_TOKEN_URI = "https://oauth2.googleapis.com/token";

Deno.serve(async (request: Request): Promise<Response> => {
  if (request.method !== "POST") {
    return json({ error: "method not allowed" }, 405);
  }

  let payload: WebhookPayload;
  try {
    payload = (await request.json()) as WebhookPayload;
  } catch {
    return json({ error: "invalid json" }, 400);
  }

  const record = payload.record;
  if (payload.type !== "INSERT" || payload.table !== "alarm_invitations" || !record) {
    return json({ skipped: "not an alarm_invitations insert" });
  }
  if (record.status !== "pending") {
    return json({ skipped: `status ${record.status}` });
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  const serviceAccountRaw = Deno.env.get("FCM_SERVICE_ACCOUNT");
  if (!supabaseUrl || !serviceRoleKey) {
    return json({ error: "SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY missing" }, 500);
  }
  if (!serviceAccountRaw) {
    return json({ error: "FCM_SERVICE_ACCOUNT missing" }, 500);
  }

  const serviceAccount = JSON.parse(serviceAccountRaw) as ServiceAccount;

  const profiles = await fetchProfiles(supabaseUrl, serviceRoleKey, [
    record.target_id,
    record.requester_id,
  ]);
  const target = profiles.find((profile) => profile.id === record.target_id);
  const requester = profiles.find((profile) => profile.id === record.requester_id);

  const pushToken = target?.push_token?.trim();
  if (!pushToken) {
    return json({ skipped: "target has no push_token" });
  }

  const accessToken = await mintAccessToken(serviceAccount);
  const fcmResponse = await fetch(
    `https://fcm.googleapis.com/v1/projects/${serviceAccount.project_id}/messages:send`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify({
        message: {
          token: pushToken,
          android: { priority: "high" },
          data: {
            type: "alarm_invitation",
            invitation_id: record.id,
            requester_nickname: requester?.nickname ?? "",
            label: record.label ?? "",
          },
        },
      }),
    },
  );

  const fcmBody = await fcmResponse.text();
  if (!fcmResponse.ok) {
    console.error("FCM send failed", fcmResponse.status, fcmBody);
    return json({ error: "fcm send failed", status: fcmResponse.status }, 502);
  }
  return json({ sent: true });
});

type ProfileRow = {
  id: string;
  nickname: string | null;
  push_token: string | null;
};

async function fetchProfiles(
  supabaseUrl: string,
  serviceRoleKey: string,
  ids: string[],
): Promise<ProfileRow[]> {
  const idList = ids.map((id) => `"${id}"`).join(",");
  const url = `${supabaseUrl.replace(/\/$/, "")}/rest/v1/user_profiles?id=in.(${idList})&select=id,nickname,push_token`;
  const response = await fetch(url, {
    headers: {
      apikey: serviceRoleKey,
      Authorization: `Bearer ${serviceRoleKey}`,
    },
  });
  if (!response.ok) {
    throw new Error(`user_profiles lookup failed: ${response.status}`);
  }
  return (await response.json()) as ProfileRow[];
}

// --- Google OAuth2 service-account JWT flow (RS256, Deno WebCrypto) ---

async function mintAccessToken(account: ServiceAccount): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  const tokenUri = account.token_uri ?? DEFAULT_TOKEN_URI;
  const header = base64UrlEncode(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const claims = base64UrlEncode(
    JSON.stringify({
      iss: account.client_email,
      scope: FCM_SCOPE,
      aud: tokenUri,
      iat: now,
      exp: now + 3600,
    }),
  );
  const unsigned = `${header}.${claims}`;
  const key = await importPrivateKey(account.private_key);
  const signature = await crypto.subtle.sign(
    { name: "RSASSA-PKCS1-v1_5" },
    key,
    new TextEncoder().encode(unsigned),
  );
  const assertion = `${unsigned}.${base64UrlEncode(new Uint8Array(signature))}`;

  const response = await fetch(tokenUri, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion,
    }),
  });
  if (!response.ok) {
    throw new Error(`token exchange failed: ${response.status} ${await response.text()}`);
  }
  const body = (await response.json()) as { access_token?: string };
  if (!body.access_token) {
    throw new Error("token exchange returned no access_token");
  }
  return body.access_token;
}

async function importPrivateKey(pem: string): Promise<CryptoKey> {
  const stripped = pem
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replace(/\s+/g, "");
  const der = Uint8Array.from(atob(stripped), (char) => char.charCodeAt(0));
  return crypto.subtle.importKey(
    "pkcs8",
    der,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"],
  );
}

function base64UrlEncode(input: string | Uint8Array): string {
  const bytes = typeof input === "string" ? new TextEncoder().encode(input) : input;
  let binary = "";
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}
