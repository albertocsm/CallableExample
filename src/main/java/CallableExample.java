import java.util.concurrent.Callable;

public class CallableExample implements Callable {

    private String word;

    public CallableExample(String word) {
        this.word = word;
    }

    public Integer call() {
        return Integer.valueOf(word.length());
    }
}

