package com.htrpc.config;

import com.htrpc.IdGenerator;
import com.htrpc.ProtocalConfig;
import com.htrpc.compress.Compressor;
import com.htrpc.compress.impl.GzipCompressor;
import com.htrpc.discovery.RegistryConfig;
import com.htrpc.loadbalancer.LoadBalancer;
import com.htrpc.loadbalancer.impl.ConsisentHashBalancer;
import com.htrpc.serialize.Serializer;
import com.htrpc.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * 全局配置类 代码配置 --》 xml配置 --》 默认
 */

@Slf4j
@Data
public class Configuration {
    //端口
    private int port = 8091;
    private String appName = "default"; //应用名称
    private RegistryConfig registryConfig;
    private ProtocalConfig protocalConfig;
    private final IdGenerator idGenerator = new IdGenerator(1,2);
    private String serializeType = "jdk";
    private String compressType = "gzip";
    private LoadBalancer loadBalancer = new ConsisentHashBalancer();

    private Compressor compressor = new GzipCompressor();
    private Serializer serializer = new JdkSerializer();

    //读xml

    public Configuration() {
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);
        loadFromXml(this);
    }

    /**
     * 从配置文件读取配置信息
     * @param configuration 实例
     */
    private void loadFromXml(Configuration configuration) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("htrpc.xml");
            Document doc = builder.parse(inputStream);


            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            String expression = "/configuration/serializer";
           Serializer serializer =  parseObject(doc, xPath,expression,null);
            System.out.println(serializer);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e){
            log.info("文件解析时发生异常",e);
        }
    }

    private <T> T parseObject(Document doc, XPath xPath,String expression,Class<?>[] paramType,Object... param) throws XPathExpressionException {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instant = null;
            if(paramType == null){
                instant = aClass.getConstructor().newInstance();
            }else{
                instant = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T)instant;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
           log.error("发生错误");
        }
        return null;
    }

    private String parseString(Document doc, XPath xPath,String expression,String AttributeName)  {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem(AttributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("发生错误");
        }
        return null;
    }

    private String parseString(Document doc, XPath xPath,String expression)  {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
             return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("发生错误");
        }
        return null;
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }

    //进行配置

}
