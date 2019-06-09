import com.google.common.base.Splitter;
import org.junit.Test;

import java.util.List;

public class split {

    @Test
    public void test() {
        List<String> bookList = Splitter.on(",").splitToList("1,2,3");
        System.out.println(bookList);
    }
}
