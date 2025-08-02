import io.github.tcq1007.valid.errorcode.ValidErrorCode;

@ValidErrorCode(prefix = "1122", length = 8, codeField = "code")
public enum TestEnum {
    SUCCESS(11220001, "Operation successful"),
    PARAM_ERROR(11220002, "Parameter error"),
    SYSTEM_ERROR(11220003, "System error");

    private final int code;
    private final String message;

    TestEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}