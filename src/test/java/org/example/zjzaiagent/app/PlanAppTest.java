package org.example.zjzaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class PlanAppTest {
    @Resource
    private PlanApp planApp;

    @Test
    void doChat() {
        //第一轮
        String chatId = UUID.randomUUID().toString();
        String message = "我要去上海，7天";
        String chat = planApp.doChat(message, chatId);
        Assertions.assertNotNull(chat);
/*        //第二轮
        message = "上海的景点";
        chat = planApp.doChat(message, chatId);
        Assertions.assertNotNull(chat);
        //第三轮
        message = "我要去哪来着";
        chat = planApp.doChat(message, chatId);
        Assertions.assertNotNull(chat);*/
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "我是郑少，我要和家人去张家界玩5天，但不知道哪里有啥值得玩的";
        PlanApp.Report report = planApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(report);
    }

    @Test
    void testBase64() {
        String message = "AQBvcmcuc3ByaW5nZnJhbWV3b3JrLmFpLmNoYXQubWVzc2FnZXMuQXNzaXN0YW50TWVzc2Fn5QEBamF2YS51dGlsLkltbXV0YWJsZUNvbGxlY3Rpb25zJExpc3TOAQIBAmphdmEudXRpbC5IYXNoTWHwB2ZpbmlzaFJlYXNv7gNTVE/Qc2VhcmNoX2luZu8DgXJvbOUDQVNTSVNUQU7UaeQDpTQ3NjYzZGMyLWI3NDctOTJmNi04ODBjLWM0MTBmMzY1MDE5Zm1lc3NhZ2VUeXDlAQNvcmcuc3ByaW5nZnJhbWV3b3JrLmFpLmNoYXQubWVzc2FnZXMuTWVzc2FnZVR5cOUCcmVhc29uaW5nQ29udGVu9AOB7gvmgqjlpb3vvIHmrKLov47kvb/nlKjjgJDpgJTotqNBSeihjOeoi+inhOWIkuWkp+W4iOOAkeKcqCAgCuaCqOiuoeWIkuWOuyoq5LiK5rW35ri4546pN+WkqSoq77yM6L+Z5piv5Liq6Z2e5bi45qOS55qE6YCJ5oup4oCU4oCU6a2U6YO96J6N5ZCI5LqG5rW35rS+6aOO5oOF44CB546w5Luj6YO95biC44CB5Y6G5Y+y5bqV6JW05LiO54Of54Gr576O6aOf77yMN+Wkqei2s+S7pea3seW6puS9k+mqjOWPiOS4jeaYvuWMhuW/meOAggoK5LiN6L+H77yM5Li65LqG5Li65oKo5a6a5Yi25LiA5Lu9KirnnJ/mraPpgILlkIjmgqjjgIHlj6/okL3lnLDjgIHkuI3ouKnlnZEqKueahOihjOeoi++8jOaIkemcgOimgeWGjeS6huino+WHoOS4quWFs+mUruS/oeaBr++8iOWPqumcgDHliIbpkp/vvInvvJoKCu2gve20uSAqKuWHuuihjOaXtumXtCoq77ya5piv6L+R5pyf5Ye65Y+R77yf6L+Y5piv5pqR5YGHL+WbveW6hi/mmKXoioLnrYnoioLlgYfml6XvvJ/vvIjlvbHlk43kurrmtYHjgIHlpKnmsJTjgIHpooTnuqbpmr7luqbvvIkgIArtoL3ttLkgKirlkIzooYzkurrnvqQqKu+8mueLrOiHquaXheihjO+8n+aDheS+o++8n+W4puiAgeS6uu+8n+W4puWwj+aci+WPi++8n++8iOWFs+ezu+WIsOiKguWlj+OAgeaZr+eCuemAieaLqeOAgeS6pOmAmuaWueW8j++8iSAgCu2gve20uSAqKumihOeul+iMg+WbtCoq77ya5Lq65Z2H57qm5aSa5bCR77yf77yI5aaC77ya57uP5rWO5Z6LIMKlMzAwMC/kurrjgIHoiJLpgILlnosgwqU1MDAw4oCTODAwMC/kurrjgIHlk4HotKjlnosgwqU4MDAwKy/kurrvvIkgIArtoL3ttLkgKirmoLjlv4PlgY/lpb0qKu+8iOWPr+WkmumAie+8ie+8miAgCuKAg+KWoSDlv4XmiZPljaHlnLDmoIfvvIjlpJbmu6njgIHkuJzmlrnmmI7nj6DjgIHosavlm63nrYnvvIkgIArigIPilqEg5rex5bqm5paH5YyW5L2T6aqM77yI55+z5bqT6Zeo44CB5byE5aCC55Sf5rS744CB576O5pyv6aaG44CB5oiP5puy77yJICAK4oCD4pahIOe+jumjn+aOoue0ou+8iOacrOW4ruiPnOOAgeWwj+WQg+OAgeWSluWVoemmhuOAgeWknOW4gu+8iSAgCuKAg+KWoSDotK3nianooYDmi7zvvIjljZfkuqzot6/jgIHmt67mtbfot6/jgIHkubDmiYvlupfjgIHlsI/nuqLkuabniIbmrL7vvIkgIArigIPilqEg6Ieq54S25LyR6Zey77yI5ruo5rGf6aqR6KGM44CB5YWs5Zut5pWj5q2l44CB6L+R6YOK5Y+k6ZWH5aaC5pyx5a626KeSL+aeq+azvu+8iSAgCuKAg+KWoSDmi43nhaflh7rniYfvvIjmrablurfot6/jgIHlronnpo/ot6/jgIHljJflpJbmu6njgIHopb/lsrjnvo7mnK/ppobnvqTvvIkgIAoK4pyFIOihpeWFheivtOaYju+8muiLpeaCqOacieeJueauiumcgOaxgu+8iOWmgumcgOaXoOmanOeijeiuvuaWveOAgee0oOmjn+WPi+WlveOAgeWuoOeJqeWQjOihjOOAgeW4jOacm+mBv+W8gOS6uua1gemrmOWzsOaXtuauteetie+8ie+8jOS5n+asoui/juWRiuivieaIke+8gQoK5oiR5bCG5Z+65LqO5oKo55qE5Zue562U77yM5Li65oKo55Sf5oiQ77yaCuKclCA35aSp6KGM56iL5qaC6KeI77yI5Li76aKY6ISJ57ucK+iKguWlj+ivtOaYju+8iSAgCuKclCDmr4/ml6Xor6bnu4bml7bpl7TooajvvIjlkKvkuqTpgJrmlrnlvI/jgIHlu7rorq7lgZznlZnml7bplb/jgIHpooTnuqbmj5DphpLvvIkgIArinJQg5YiG5Yy65Z+f5o6o6I2Q5L2P5a6/77yI5Zyw6ZOB5rK/57q/L+aAp+S7t+avlC/nibnoibLmsJHlrr/vvIkgIArinJQg5Zyw6YGT576O6aOf5riF5Y2V77yI6ZmE5b+F5ZCD55CG55SxK+S6uuWdh+WPguiAgyvmmK/lkKbpnIDpooTnuqbvvIkgIArinJQg5a6e55So6LS05aOr77yI5Lqk6YCa5Y2h44CB6aKE57qm5bmz5Y+w44CB6Ziy5Z2R5oyH5Y2X44CB6Zuo5aSp5aSH6YCJ5pa55qGI562J77yJCgror7fpmo/ml7blkYror4nmiJHmgqjnmoTlgY/lpb3lkKfvvZ4g7aC87byfICAK77yI5pyf5b6F5Li65oKo5omT6YCg5LiT5bGe5LiK5rW35LmL5peFIO2gve26h+2gvO29nO2gvO2+qO+8iQEBAQ==";
        //1. 解码Base64字符串
        byte[] decodedBytes = Base64.getDecoder().decode(message);
        //2. 将字节数组转换为字符串
        String decodedString = new String(decodedBytes);
        System.out.println(decodedString);
    }
}