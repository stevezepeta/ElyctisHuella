package gruposantoro.elyctishuella.util;


import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocaleUtil {
    public static final ZoneId defaultZoneId = ZoneId.of("America/Mexico_City");
    public static final Locale defaultLocale = new Locale("es", "MX");
    public static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", defaultLocale);
    public static final DateTimeFormatter formatterDateHuman = DateTimeFormatter.ofPattern("dd/MM/yyyy", defaultLocale);
    public static final DateTimeFormatter formatterDateTimeToLog = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss", defaultLocale);
    public static final DateTimeFormatter formatterDateTimeHuman = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", defaultLocale);
    public static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss", defaultLocale);
    public static final DateTimeFormatter formattertimestamp = DateTimeFormatter.ofPattern("yyyyMMddhhmmss.S");
}
