/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.hanborq.gravity.metrics.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TMetricsServer {

  public interface Iface {

    public void pushMetrics(List<TMetricsRecord> records) throws org.apache.thrift.TException;

  }

  public interface AsyncIface {

    public void pushMetrics(List<TMetricsRecord> records, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.pushMetrics_call> resultHandler) throws org.apache.thrift.TException;

  }

  public static class Client extends org.apache.thrift.TServiceClient implements Iface {
    public static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {
      public Factory() {}
      public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
        return new Client(prot);
      }
      public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
        return new Client(iprot, oprot);
      }
    }

    public Client(org.apache.thrift.protocol.TProtocol prot)
    {
      super(prot, prot);
    }

    public Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
      super(iprot, oprot);
    }

    public void pushMetrics(List<TMetricsRecord> records) throws org.apache.thrift.TException
    {
      send_pushMetrics(records);
    }

    public void send_pushMetrics(List<TMetricsRecord> records) throws org.apache.thrift.TException
    {
      pushMetrics_args args = new pushMetrics_args();
      args.setRecords(records);
      sendBase("pushMetrics", args);
    }

  }
  public static class AsyncClient extends org.apache.thrift.async.TAsyncClient implements AsyncIface {
    public static class Factory implements org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {
      private org.apache.thrift.async.TAsyncClientManager clientManager;
      private org.apache.thrift.protocol.TProtocolFactory protocolFactory;
      public Factory(org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
        this.clientManager = clientManager;
        this.protocolFactory = protocolFactory;
      }
      public AsyncClient getAsyncClient(org.apache.thrift.transport.TNonblockingTransport transport) {
        return new AsyncClient(protocolFactory, clientManager, transport);
      }
    }

    public AsyncClient(org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.transport.TNonblockingTransport transport) {
      super(protocolFactory, clientManager, transport);
    }

    public void pushMetrics(List<TMetricsRecord> records, org.apache.thrift.async.AsyncMethodCallback<pushMetrics_call> resultHandler) throws org.apache.thrift.TException {
      checkReady();
      pushMetrics_call method_call = new pushMetrics_call(records, resultHandler, this, ___protocolFactory, ___transport);
      this.___currentMethod = method_call;
      ___manager.call(method_call);
    }

    public static class pushMetrics_call extends org.apache.thrift.async.TAsyncMethodCall {
      private List<TMetricsRecord> records;
      public pushMetrics_call(List<TMetricsRecord> records, org.apache.thrift.async.AsyncMethodCallback<pushMetrics_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
        super(client, protocolFactory, transport, resultHandler, true);
        this.records = records;
      }

      public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("pushMetrics", org.apache.thrift.protocol.TMessageType.CALL, 0));
        pushMetrics_args args = new pushMetrics_args();
        args.setRecords(records);
        args.write(prot);
        prot.writeMessageEnd();
      }

      public void getResult() throws org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
      }
    }

  }

  public static class Processor<I extends Iface> extends org.apache.thrift.TBaseProcessor<I> implements org.apache.thrift.TProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());
    public Processor(I iface) {
      super(iface, getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
    }

    protected Processor(I iface, Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      super(iface, getProcessMap(processMap));
    }

    private static <I extends Iface> Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> getProcessMap(Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      processMap.put("pushMetrics", new pushMetrics());
      return processMap;
    }

    public static class pushMetrics<I extends Iface> extends org.apache.thrift.ProcessFunction<I, pushMetrics_args> {
      public pushMetrics() {
        super("pushMetrics");
      }

      public pushMetrics_args getEmptyArgsInstance() {
        return new pushMetrics_args();
      }

      protected boolean isOneway() {
        return true;
      }

      public org.apache.thrift.TBase getResult(I iface, pushMetrics_args args) throws org.apache.thrift.TException {
        iface.pushMetrics(args.records);
        return null;
      }
    }

  }

  public static class pushMetrics_args implements org.apache.thrift.TBase<pushMetrics_args, pushMetrics_args._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("pushMetrics_args");

    private static final org.apache.thrift.protocol.TField RECORDS_FIELD_DESC = new org.apache.thrift.protocol.TField("records", org.apache.thrift.protocol.TType.LIST, (short)1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new pushMetrics_argsStandardSchemeFactory());
      schemes.put(TupleScheme.class, new pushMetrics_argsTupleSchemeFactory());
    }

    public List<TMetricsRecord> records; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      RECORDS((short)1, "records");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: // RECORDS
            return RECORDS;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments
    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.RECORDS, new org.apache.thrift.meta_data.FieldMetaData("records", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
              new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TMetricsRecord.class))));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(pushMetrics_args.class, metaDataMap);
    }

    public pushMetrics_args() {
    }

    public pushMetrics_args(
      List<TMetricsRecord> records)
    {
      this();
      this.records = records;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public pushMetrics_args(pushMetrics_args other) {
      if (other.isSetRecords()) {
        List<TMetricsRecord> __this__records = new ArrayList<TMetricsRecord>();
        for (TMetricsRecord other_element : other.records) {
          __this__records.add(new TMetricsRecord(other_element));
        }
        this.records = __this__records;
      }
    }

    public pushMetrics_args deepCopy() {
      return new pushMetrics_args(this);
    }

    @Override
    public void clear() {
      this.records = null;
    }

    public int getRecordsSize() {
      return (this.records == null) ? 0 : this.records.size();
    }

    public java.util.Iterator<TMetricsRecord> getRecordsIterator() {
      return (this.records == null) ? null : this.records.iterator();
    }

    public void addToRecords(TMetricsRecord elem) {
      if (this.records == null) {
        this.records = new ArrayList<TMetricsRecord>();
      }
      this.records.add(elem);
    }

    public List<TMetricsRecord> getRecords() {
      return this.records;
    }

    public pushMetrics_args setRecords(List<TMetricsRecord> records) {
      this.records = records;
      return this;
    }

    public void unsetRecords() {
      this.records = null;
    }

    /** Returns true if field records is set (has been assigned a value) and false otherwise */
    public boolean isSetRecords() {
      return this.records != null;
    }

    public void setRecordsIsSet(boolean value) {
      if (!value) {
        this.records = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case RECORDS:
        if (value == null) {
          unsetRecords();
        } else {
          setRecords((List<TMetricsRecord>)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case RECORDS:
        return getRecords();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case RECORDS:
        return isSetRecords();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof pushMetrics_args)
        return this.equals((pushMetrics_args)that);
      return false;
    }

    public boolean equals(pushMetrics_args that) {
      if (that == null)
        return false;

      boolean this_present_records = true && this.isSetRecords();
      boolean that_present_records = true && that.isSetRecords();
      if (this_present_records || that_present_records) {
        if (!(this_present_records && that_present_records))
          return false;
        if (!this.records.equals(that.records))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(pushMetrics_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      pushMetrics_args typedOther = (pushMetrics_args)other;

      lastComparison = Boolean.valueOf(isSetRecords()).compareTo(typedOther.isSetRecords());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetRecords()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.records, typedOther.records);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("pushMetrics_args(");
      boolean first = true;

      sb.append("records:");
      if (this.records == null) {
        sb.append("null");
      } else {
        sb.append(this.records);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
      // check for sub-struct validity
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private static class pushMetrics_argsStandardSchemeFactory implements SchemeFactory {
      public pushMetrics_argsStandardScheme getScheme() {
        return new pushMetrics_argsStandardScheme();
      }
    }

    private static class pushMetrics_argsStandardScheme extends StandardScheme<pushMetrics_args> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, pushMetrics_args struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 1: // RECORDS
              if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                {
                  org.apache.thrift.protocol.TList _list30 = iprot.readListBegin();
                  struct.records = new ArrayList<TMetricsRecord>(_list30.size);
                  for (int _i31 = 0; _i31 < _list30.size; ++_i31)
                  {
                    TMetricsRecord _elem32; // required
                    _elem32 = new TMetricsRecord();
                    _elem32.read(iprot);
                    struct.records.add(_elem32);
                  }
                  iprot.readListEnd();
                }
                struct.setRecordsIsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            default:
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
          }
          iprot.readFieldEnd();
        }
        iprot.readStructEnd();

        // check for required fields of primitive type, which can't be checked in the validate method
        struct.validate();
      }

      public void write(org.apache.thrift.protocol.TProtocol oprot, pushMetrics_args struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.records != null) {
          oprot.writeFieldBegin(RECORDS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.records.size()));
            for (TMetricsRecord _iter33 : struct.records)
            {
              _iter33.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class pushMetrics_argsTupleSchemeFactory implements SchemeFactory {
      public pushMetrics_argsTupleScheme getScheme() {
        return new pushMetrics_argsTupleScheme();
      }
    }

    private static class pushMetrics_argsTupleScheme extends TupleScheme<pushMetrics_args> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, pushMetrics_args struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetRecords()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetRecords()) {
          {
            oprot.writeI32(struct.records.size());
            for (TMetricsRecord _iter34 : struct.records)
            {
              _iter34.write(oprot);
            }
          }
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, pushMetrics_args struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          {
            org.apache.thrift.protocol.TList _list35 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
            struct.records = new ArrayList<TMetricsRecord>(_list35.size);
            for (int _i36 = 0; _i36 < _list35.size; ++_i36)
            {
              TMetricsRecord _elem37; // required
              _elem37 = new TMetricsRecord();
              _elem37.read(iprot);
              struct.records.add(_elem37);
            }
          }
          struct.setRecordsIsSet(true);
        }
      }
    }

  }

}
