package com.test.stress;

import com.test.common.HttpClient;
import com.test.utils.JSONParaser;
import com.test.utils.Tools;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterVariables;

public class JHttpRequestSample extends AbstractJavaSamplerClient {
    /**
     * Get default parameter from the java request sampler.
     * @return arguments
     */
    @Override
    /**
     * getDefaultParameters：获取java request的参数（默认参数）
     */
    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        params.addArgument("hostname","default_host");
        params.addArgument("hostname_1","default_host");
        return params;
    }

    /**
     * Pre-test method. (Optional)
     * @param context
     * setupTest：设置一下测试
     */
    public void setupTest(JavaSamplerContext context) {
        System.out.println("==== Restful API test started! ====");
    }

    /**
     * Post-test method. (Optional)
     *
     * @param context
     * teardownTest：完成测试
     */
    public void teardownTest(JavaSamplerContext context) {
        System.out.println("==== Restful API test stopped. ====");
    }

    /**
     * Test running method. (Required)
     *
     * @param arg0
     * @return
     * runTest：运行测试时，整个函数会被运行（函数是必须的，没有会报错，上面3个不是必须的）
     */
    public SampleResult runTest(JavaSamplerContext arg0) {
        /**
         * 从JMeter的全局变量中获取参数值
         * JavaSamplerContext：上下文变量，会把参数实例传给arg0
         * arg0进入实体后，通过arg0.getJMeterVariables()能够获取jmeter内部的全局变量
         * port：api的接口    用户名、密码
         */
        JMeterVariables jMeterVariables = arg0.getJMeterVariables();
        String port = jMeterVariables.get("port");
        String username = jMeterVariables.get("username");
        String password = jMeterVariables.get("password");

        /**
         * 从Java Request变量列表中获取参数值
         * arg0.getParameter：java request的局部变量
         * hostname：localhost
         */
        String hostname = arg0.getParameter("hostname");

        System.out.println("== JMeter Variables:");
        System.out.println("hostname = " + hostname);
        System.out.println("port = " + port);
        System.out.println("username = " + username);
        System.out.println("password = " + password);

        System.out.println("== Test Start with user " + username);
        /**
         * 测试程序，为了记录测试结果，所以创建一个SampleResult对象
         * SampleResult：可以被jmeter识别的对象
         */
        SampleResult sampleResult = new SampleResult();
        /**
         * sampleStart()：为了记录这次压测对这个接口产生交互之前的时间点
         * 访问结束后再记录访问之后的时间点，2个时间差就是接口访问的时间
         * 哪个sample快哪个sample慢就知道了
         */
        sampleResult.sampleStart();
        /**
         * 运行接口测试组合代码，接口1 -> 接口4
         * menuRestfulAPITest：函数，例子封装好的一个接口测试函数
         * 所有接口测试动作都会放到这个函数里，封装后供其他函数调用的一个子函数
         * 返回值是布尔类型
         * setSuccessful子函数的目的是为了把java request sampler本身的交互结果设置成true、flase
         * true：对这个接口访问的结果设置为成功
         * flase：对这个接口访问的结果设置为失败
         * flase：对这个接口访问的结果设置为失败
         */
        boolean testResult = menuRestfulAPITest(hostname, port, username, password);
        if (testResult) {
            sampleResult.setSuccessful(true); //设定成功条件下的Java Request 结果为成功
            String succMsg = "Menu restfulAPI test success.";
            sampleResult.setResponseData(succMsg.getBytes());
            System.out.println(succMsg);

        } else {
            sampleResult.setSuccessful(false); //设定成功条件下的Java Request 结果为失败
            String failMsg = "Menu restfulAPI test failed.";
            sampleResult.setResponseData(failMsg.getBytes());
            System.out.println(failMsg);
        }
        /**
         * sampleEnd：请求结束的时候打个点
         */
        sampleResult.sampleEnd();
        return sampleResult;
    }

    /**
     * main函数为了调试使用
     */
    public static void main(String[] args) {
        String hostname = "localhost";
        String port = "9091";
        String username = "user01";
        String password = "pwd";
        boolean result = JHttpRequestSample.menuRestfulAPITest(hostname, port, username, password);
        System.out.println(result);

        String id = Tools.createIdcard();

        String d = Tools.getCurrentTime();

        System.out.println(id);

        System.out.println(d);
    }

    public static boolean menuRestfulAPITest(String hostname, String port, String username, String password) {
        String protocol = "http";
        String access_token = "";
        boolean result = true;

        //接口1 登录操作
        String path1 = "/api/v1/user/login";
        String url1 = protocol + "://" + hostname + ":" + port + path1;
        String reqData1 = "{\n" +
                "\t\"authRequest\": {\n" +
                "\t    \"userName\": \"" + username + "\",\n" +
                "\t    \"password\": \"" + password + "\"\n" +
                "\t}\n" +
                "}";
        String respData1 = HttpClient.sendPost(url1, reqData1, access_token);
        access_token = JSONParaser.getJsonValue(respData1, "access_token");
        String retcode1 = JSONParaser.getJsonValue(respData1, "code");
        if (!"200".equalsIgnoreCase(retcode1)) {  //校验接口1的返回code是否等于200
            /**
             * 防止空指针异常的小技巧
             * retcode1.equalsIgnoreCase("200")
             * "200".equalsIgnoreCase(retcode1)
             * 2种方式，因为假设retcode1为空的话，retcode1.equalsIgnoreCase("200")就会报错
             * 但是"200".equalsIgnoreCase(retcode1)不会报错，一个已经实例化的字符串调用函数就不会再报空指针异常
             */
            result = false;
        }
        System.out.println(respData1);

        //接口2 浏览菜单
        String path2 = "/api/v1/menu/list";
        String url2 = protocol + "://" + hostname + ":" + port + path2;
        String respData2 = HttpClient.sendGet(url2, access_token);
        String retcode2 = JSONParaser.getJsonValue(respData2, "code");
        if (!"200".equalsIgnoreCase(retcode2)) { //校验接口2的返回code是否等于200
            result = false;
        }
        System.out.println(respData2);

        //接口3 下订单
        String path3 = "/api/v1/menu/confirm";
        String url3 = protocol + "://" + hostname + ":" + port + path3;
        String reqData3 = "{\n" +
                "    \"order_list\": [\n" +
                "        {\n" +
                "            \"menu_nunber\" : \"01\",\n" +
                "            \"number\" : 1\n" +
                "        },\n" +
                "        {\n" +
                "            \"menu_nunber\" : \"03\",\n" +
                "            \"number\" : 2\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        String respData3 = HttpClient.sendPost(url3, reqData3, access_token);
        String retcode3 = JSONParaser.getJsonValue(respData3, "code");
        if (!"200".equalsIgnoreCase(retcode3)) { //校验接口3的返回code是否等于200
            result = false;
        }
        System.out.println(respData3);

        //接口4 退出
        String path4 = "/api/v1/user/logout";
        String url4 = protocol + "://" + hostname + ":" + port + path4;
        String respData4 = HttpClient.sendDelete(url4, access_token);
        String retcode4 = JSONParaser.getJsonValue(respData4, "code");

        if (!"200".equalsIgnoreCase(retcode4)) { //校验接口4的返回code是否等于200
            result = false;
        }
        System.out.println(respData4);
        return result;
    }

}
