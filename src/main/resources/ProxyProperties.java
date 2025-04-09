// package xyz.idoly.comic.config;

// import org.springframework.boot.context.properties.ConfigurationProperties;

// @ConfigurationProperties(prefix = "spring.proxy")
// public class ProxyProperties {

//     public enum Mode {
//         AUTO, MANUAL, NONE
//     }

//     private Mode mode = Mode.NONE;

//     private String host;
//     private int port;

//     public Mode getMode() {
//         return mode;
//     }

//     public void setMode(Mode mode) {
//         this.mode = mode;
//     }

//     public String getHost() {
//         return host;
//     }

//     public void setHost(String host) {
//         this.host = host;
//     }

//     public int getPort() {
//         return port;
//     }

//     public void setPort(int port) {
//         this.port = port;
//     }

//     public String getProxy() {
//         return this.host + ":" + this.port;
//     }
 
//     @Override
//     public String toString() {
//         return "ProxyProperties [mode=" + mode + ", host=" + host + ", port=" + port + "]";
//     }
// }
