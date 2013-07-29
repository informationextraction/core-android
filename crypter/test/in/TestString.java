
public class TestString {
	String saluti = M.e("Ciao mondo");
	String saluti = M.d("mistake");
	TestString(){
		System.out.println(saluti);
		System.out.println(M.e("ciao") + M.e(" sbando"
				));
	}
}
