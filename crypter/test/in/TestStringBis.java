import com.android.M;

public class TestString {
	String saluti = M.e("Hello world");
	TestString(){
		System.out.println(saluti);
		System.out.println(M.e("hello") + M.e(" again"));
	}
}
