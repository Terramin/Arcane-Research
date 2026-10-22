package thaumictinker.client.tablet;

 



 
@FunctionalInterface
public interface TabletSink {
    void vertex(float x, float y, float z, float u, float v);
}
