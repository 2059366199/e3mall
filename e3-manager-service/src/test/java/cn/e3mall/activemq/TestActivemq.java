package cn.e3mall.activemq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class TestActivemq {
	@Test
	public void testSpringActiveMq() throws Exception {
		//初始化spring容器
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-activemq.xml");
		//从spring容器中获得JmsTemplate对象
		JmsTemplate jmsTemplate = applicationContext.getBean(JmsTemplate.class);
		//从spring容器中取Destination对象
		Destination destination = (Destination) applicationContext.getBean("queueDestination");
		//使用JmsTemplate对象发送消息。
		jmsTemplate.send(destination, new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				//创建一个消息对象并返回
				TextMessage textMessage = session.createTextMessage("spring activemq queue message");
				return textMessage;
			}
		});
	}
	
	@Test
	public void testQueueProducer() throws Exception {
		// 第一步：初始化一个spring容器
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-activemq.xml");
		// 第二步：从容器中获得JMSTemplate对象。
		JmsTemplate jmsTemplate = applicationContext.getBean(JmsTemplate.class);
		// 第三步：从容器中获得一个Destination对象
		Queue queue = (Queue) applicationContext.getBean("queueDestination");
		// 第四步：使用JMSTemplate对象发送消息，需要知道Destination
		jmsTemplate.send(queue, new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage textMessage = session.createTextMessage("spring 2344");
				return textMessage;
			}
		});
	}
	
	@Test
	public void excute(){
		List<String> l1 = new ArrayList<String>();  
        l1.add("a");  
        l1.add("b");  
        l1.add("c");  
        l1.add("e");  
        l1.add("e");  
        l1.add("e");  
        l1.add("e");  
        l1.add("a");  
        List<String> listWithoutDup = new ArrayList<String>(new HashSet<String>(l1)); 
        System.out.println(new HashSet<String>(l1));
	}
	
	@Test
	public void excute1(){
		// TODO Auto-generated method stub  
        List<String> l1 = new ArrayList<String>();  
        l1.add("a");  
        l1.add("a");  
        l1.add("c");  
        l1.add("c");  
          
        List<String> l2 = new ArrayList<String>();  
        l2.add("b");  
        l2.add("b");  
        l2.add("k");  
        l2.add("k");  
          
        l1.removeAll(l2);//此处指的是将与l2重复的删除  
        l1.addAll(l2);//此处指加上l2  
          
        //如果保证l1,和l2  2个各自的LIST 本身不重复，此行代码不用写。否则会出现合并后LIST重复的问题，具体看业务需要  
        l1 = new ArrayList<String>(new HashSet<>(l1));  
        Collections.sort(l1);
        for(String str : l1){  
            System.out.println(str);  
        }  
          
	}
	@Test
	public void excute2(){
		// TODO Auto-generated method stub
		 Scanner scanner = new Scanner(System.in);
		 System.out.print("请输入小写数字：");
		 int number = scanner.nextInt();
		 String res = ""; 
		 res = change(number);
		 System.out.println("转换为大写数字："+res);
	}
	
	 public static String change(int number){
		 String[] bigwrite = {"零","壹","贰","叁","肆","伍","陆","柒","捌","玖"};
		 char[] str = String.valueOf(number).toCharArray(); 
		 //将整型转化为字符型，通过调用toCharArray方法接收字符串
		 String rstr = "";//用来接收字符的数组 
		 for (int i = 0; i <str.length; i++) { 
			 rstr = rstr + bigwrite[Integer.parseInt(str[i] + "")]; 
		 } 
		 /*
		 * Integer.parseInt(str[i] + "")字符串下标为i的字符转换成整型
		 */
		 return rstr; 
	}
	
	 @Test
	 public void exe(){
		 int tickets = 10;
		 if(tickets >= 0){
			 System.out.println(tickets --);
		 }
		 //System.out.println(String.valueOf(23445365).toCharArray());
	 }
}
