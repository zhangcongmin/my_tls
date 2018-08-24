package cn.talianshe.android;

import org.junit.Test;

import cn.talianshe.android.utils.Base64;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testGenerateBase64() throws Exception {
        String str = "1513068169742";
        String base64 = Base64.getBase64(str);
        System.out.println(base64);
    }
}