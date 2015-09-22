package org.ehcache.demo.serializer;

import org.ehcache.exceptions.SerializerException;
import org.ehcache.spi.serialization.Serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

// tag::transientSerializer[]
@Serializer.Transient
public class DumbTransientStringSerializer implements Serializer<String> {

  protected Map<Integer, String> idStringMap = new HashMap<Integer, String>();
  protected Map<String, Integer> stringIdMap = new HashMap<String, Integer>();
  protected int id = 0;

  public DumbTransientStringSerializer(ClassLoader loader) {
    //no-op
  }

  @Override
  public ByteBuffer serialize(final String object) throws SerializerException {
    Integer currentId = stringIdMap.get(object);
    if(currentId == null) {
      stringIdMap.put(object, id);
      idStringMap.put(id, object);
      currentId = id++;
    }

    ByteBuffer buff = ByteBuffer.allocate(4);
    buff.putInt(currentId).flip();
    return buff;
  }

  @Override
  public String read(final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
    Integer mapping = binary.getInt();
    String obj = idStringMap.get(mapping);
    if(obj == null) {
      throw new SerializerException("Unable to serialize: " + binary.array() + ". No value mapping found for " + mapping);
    }
    return obj;
  }

  @Override
  public boolean equals(final String object, final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
    return object.equals(read(binary));
  }

  @Override
  public void close() throws IOException {
    // no-op
  }

}
// end::transientSerializer[]
