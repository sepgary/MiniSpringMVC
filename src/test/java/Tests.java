import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.net.URL;

public class Tests {

    @Test
    public void testDom4j() {
//        SAXReader saxReader=new SAXReader();
//        //根据user.xml文档生成Document对象
//        try {
//            Document document = saxReader.read(this.getClass().getClassLoader().getResource("springmvc.xml"));
//            Element element = (Element) document.selectSingleNode("/beans/component-scan");
//            String packs = element.attributeValue("base-package");
//            System.out.println(packs);
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }

        URL url = this.getClass().getClassLoader().getResource("/" + "hello");
        System.out.println(url);
    }
}
