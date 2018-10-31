public class Main {

    public static int stringToNumber(String str){
        int cnt = str.length();
        int result = 0;
        for(int i = 0; i < cnt; i++){
            result += (int) str.charAt(i);
        }
        return result;
    }

    private static String stringToKey(String str){
        String text = "";
        int cnt = str.length() - 1;
        for(int i = 0; i < cnt; i++){
            if (i % 5 == 0 && i != 0){
                text += "-";
            }
            text += str.charAt(i);
        }
        return text;
    }

    public static void main(String args[]){
        String text = "48060-88200-12180-51420-96100".replaceAll("-", "");
        int num = stringToNumber("SRENG VANHAK");
        String text2 = "";
        int cnt = text.length();
        for(int i = 0; i < cnt; i++){
            text2 += ((int) text.charAt(i) + num);
        }
        String registerKey;
        try{
            registerKey = stringToKey(text2).substring(0,29);
        }catch(Exception ex){
            registerKey = "";
        }
        System.out.println(registerKey);
    }
}