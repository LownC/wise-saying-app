import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RqTest {
    @Test
    void actionName() {
        assertEquals("삭제", new Rq("삭제?id=1").getActionName());
    }

    @Test
    void paramAsInt() {
        assertEquals(1, new Rq("삭제?id=1").getParamAsInt("id", -1));
    }

    @Test
    void paramAsIntWithoutParam() {
        assertEquals(-1, new Rq("삭제").getParamAsInt("id", -1));
    }

    @Test
    void paramAsIntWithoutValue() {
        assertEquals(-1, new Rq("삭제?id=").getParamAsInt("id", -1));
    }

    @Test
    void paramAsIntNotNumber() {
        assertEquals(-1, new Rq("삭제?id=일번").getParamAsInt("id", -1));
    }

    @Test
    void param() {
        assertEquals("영광", new Rq("목록?searchKeyword=영광").getParam("searchKeyword", ""));
    }

    @Test
    void paramDefault() {
        assertEquals("", new Rq("목록").getParam("searchKeyword", ""));
    }

    @Test
    void multipleParams() {
        assertEquals(5, new Rq("목록?page=5&searchKeyword=영광").getParamAsInt("page", -1));
        assertEquals(5, new Rq("목록?searchKeyword=영광&page=5").getParamAsInt("page", -1));
        assertEquals("영광", new Rq("목록?page=5&searchKeyword=영광").getParam("searchKeyword", ""));
    }
}
