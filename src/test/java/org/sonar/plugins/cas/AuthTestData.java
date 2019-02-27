package org.sonar.plugins.cas;

import org.sonar.plugins.cas.util.SimpleJwt;

public class AuthTestData {
    public static final long EXPIRATION_AS_EPOCH_SECONDS = 1550331060L;
    public static final SimpleJwt JWT_TOKEN = SimpleJwt.fromIdAndExpiration("AWjne4xYY4T-z3CxdIRY", EXPIRATION_AS_EPOCH_SECONDS);

    public static String getJwtToken() {
//        {
//            "jti": "AWjne4xYY4T-z3CxdIRY",
//            "sub": "admin",
//            "iat": 1550071860,
//            "exp": 1550331060,
//            "lastRefreshTime": 1550071860312,
//            "xsrfToken": "vlmjsujqetr88ubi8k1ja7f7mo"
//        }
        return "eyJhbGciOiJIUzI1NiJ9." +
                "eyJqdGkiOiJBV2puZTR4WVk0VC16M0N4ZElSWSIsInN1YiI6ImFkbWluIiwiaWF0IjoxNTUwMDcxODYwL" +
                "CJleHAiOjE1NTAzMzEwNjAsImxhc3RSZWZyZXNoVGltZSI6MTU1MDA3MTg2MDMxMiwieHNyZlR" +
                "va2VuIjoidmxtanN1anFldHI4OHViaThrMWphN2Y3bW8ifQ." +
                "gVi7z6jEsgI3z0Y3k3oXYpIDUzMoqZd8INwvXAq_Z0E";
    }

    static String getLogoutTicket() {
        return "<samlp:LogoutRequest " +
                "       xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" " +
                "       ID=\"LR-2-aGgkCE5XTQrFeM9oFfNX2gP5hKQqVHDDczm\" " +
                "       Version=\"2.0\" " +
                "       IssueInstant=\"2019-02-28T13:06:31Z\">" +
                "   <saml:NameID " +
                "           xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">" +
                "       @NOT_USED@" +
                "   </saml:NameID>" +
                "   <samlp:SessionIndex>ST-4-F79YAkFe2SjoURpJutQ7-45aa256f981c</samlp:SessionIndex>" +
                "</samlp:LogoutRequest>";
    }
}
