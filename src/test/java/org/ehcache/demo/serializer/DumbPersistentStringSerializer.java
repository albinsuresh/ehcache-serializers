package org.ehcache.demo.serializer;

import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.service.FileBasedPersistenceContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;


// tag::persistentSerializer[]
@Serializer.Persistent
public class DumbPersistentStringSerializer extends DumbTransientStringSerializer {

  private final File stateFile;

  public DumbPersistentStringSerializer(final ClassLoader loader, FileBasedPersistenceContext persistence) throws IOException, ClassNotFoundException {
    super(loader);
    stateFile = new File(persistence.getDirectory(), "serializer.data");
    if(stateFile.exists()) {
      restoreState();
    }
  }

  @Override
  public void close() throws IOException {
    persistState();
  }

  private void restoreState() throws IOException, ClassNotFoundException {
    FileInputStream fin = new FileInputStream(stateFile);
    try {
      ObjectInputStream oin = new ObjectInputStream(fin);
      try {
        idStringMap = (Map<Integer, String>) oin.readObject();
        stringIdMap = (Map<String, Integer>) oin.readObject();
        id = oin.readInt();
      } finally {
        oin.close();
      }
    } finally {
      fin.close();
    }
  }

  private void persistState() throws IOException {
    OutputStream fout = new FileOutputStream(stateFile);
    try {
      ObjectOutputStream oout = new ObjectOutputStream(fout);
      try {
        oout.writeObject(idStringMap);
        oout.writeObject(stringIdMap);
        oout.writeInt(id);
      } finally {
        oout.close();
      }
    } finally {
      fout.close();
    }
  }
}
// end::persistentSerializer[]
