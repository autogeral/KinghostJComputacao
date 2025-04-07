package br.com.jcomputacao.kinghost;

/**
 * 29/02/2016 12:34:24
 *
 * @author murilo.lima
 */
public final class StringUtil {

    public static String noDeadKeysToUpperCase(String str) {
        if (isNull(str)) {
            return str;
        }
        return str.toUpperCase().replace("Á", "A")
                .replace("À", "A")
                .replace("Ã", "A")
                .replace("Ä", "A")
                .replace("Â", "A")
                .replace("Ç", "C")
                .replace("Ñ", "N")
                .replace("É", "E")
                .replace("Ê", "E")
                .replace("È", "E")
                .replace("Ë", "E")
                .replace("Í", "I")
                .replace("Ì", "I")
                .replace("Î", "I")
                .replace("Ï", "I")
                .replace("Ó", "O")
                .replace("Ò", "O")
                .replace("Ô", "O")
                .replace("Õ", "O")
                .replace("Ö", "O")
                .replace("Ú", "U")
                .replace("Ù", "U")
                .replace("Ü", "U")
                .replace("Û", "U")
                .replace("º", "o")
                .replace("&", "E")
                .replace("ª", "a");
    }

    public static boolean isNull(String str) {
        return (str == null || str.trim().equals(""));
    }
}
