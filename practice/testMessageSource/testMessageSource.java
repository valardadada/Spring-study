public class testMessageSource{
	public static void main(String[] args){
		MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
		String message = resources.getMessage("message", null, "Default", Locale.ENGLIST);
		System.out.println(message);
	}
}
