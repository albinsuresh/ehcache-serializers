package org.ehcache.demo.serializer;

import org.ehcache.exceptions.SerializerException;
import org.ehcache.spi.serialization.Serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by alsu on 22/09/15.
 */
// tag::thirdPartySerializer[]
public class KryoStringSerializer implements Serializer<String> {

  Kryo kryo = new Kryo();

  public KryoStringSerializer(ClassLoader loader) {
    //no-op
  }

  @Override
  public ByteBuffer serialize(final String object) throws SerializerException {
    Output output = new Output(object.length()+1);
    kryo.writeObject(output, object);
    ByteBuffer buff = ByteBuffer.wrap(output.getBuffer());
    return buff;
  }

  @Override
  public String read(final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
    byte[] bytes = new byte[binary.remaining()];
    binary.get(bytes);
    Input input = new Input(bytes);
    String obj = kryo.readObject(input, String.class);
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
// end::thirdPartySerializer[]
