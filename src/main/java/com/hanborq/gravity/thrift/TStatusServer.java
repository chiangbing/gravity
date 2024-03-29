/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.hanborq.gravity.thrift;

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

public class TStatusServer {

  public interface Iface {

    public void reportWorkStatus(TWorkStatus status) throws org.apache.thrift.TException;

  }

  public interface AsyncIface {

    public void reportWorkStatus(TWorkStatus status, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.reportWorkStatus_call> resultHandler) throws org.apache.thrift.TException;

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

    public void reportWorkStatus(TWorkStatus status) throws org.apache.thrift.TException
    {
      send_reportWorkStatus(status);
    }

    public void send_reportWorkStatus(TWorkStatus status) throws org.apache.thrift.TException
    {
      reportWorkStatus_args args = new reportWorkStatus_args();
      args.setStatus(status);
      sendBase("reportWorkStatus", args);
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

    public void reportWorkStatus(TWorkStatus status, org.apache.thrift.async.AsyncMethodCallback<reportWorkStatus_call> resultHandler) throws org.apache.thrift.TException {
      checkReady();
      reportWorkStatus_call method_call = new reportWorkStatus_call(status, resultHandler, this, ___protocolFactory, ___transport);
      this.___currentMethod = method_call;
      ___manager.call(method_call);
    }

    public static class reportWorkStatus_call extends org.apache.thrift.async.TAsyncMethodCall {
      private TWorkStatus status;
      public reportWorkStatus_call(TWorkStatus status, org.apache.thrift.async.AsyncMethodCallback<reportWorkStatus_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
        super(client, protocolFactory, transport, resultHandler, true);
        this.status = status;
      }

      public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("reportWorkStatus", org.apache.thrift.protocol.TMessageType.CALL, 0));
        reportWorkStatus_args args = new reportWorkStatus_args();
        args.setStatus(status);
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
      processMap.put("reportWorkStatus", new reportWorkStatus());
      return processMap;
    }

    public static class reportWorkStatus<I extends Iface> extends org.apache.thrift.ProcessFunction<I, reportWorkStatus_args> {
      public reportWorkStatus() {
        super("reportWorkStatus");
      }

      public reportWorkStatus_args getEmptyArgsInstance() {
        return new reportWorkStatus_args();
      }

      protected boolean isOneway() {
        return true;
      }

      public org.apache.thrift.TBase getResult(I iface, reportWorkStatus_args args) throws org.apache.thrift.TException {
        iface.reportWorkStatus(args.status);
        return null;
      }
    }

  }

  public static class reportWorkStatus_args implements org.apache.thrift.TBase<reportWorkStatus_args, reportWorkStatus_args._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("reportWorkStatus_args");

    private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.STRUCT, (short)1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new reportWorkStatus_argsStandardSchemeFactory());
      schemes.put(TupleScheme.class, new reportWorkStatus_argsTupleSchemeFactory());
    }

    public TWorkStatus status; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      STATUS((short)1, "status");

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
          case 1: // STATUS
            return STATUS;
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
      tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TWorkStatus.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(reportWorkStatus_args.class, metaDataMap);
    }

    public reportWorkStatus_args() {
    }

    public reportWorkStatus_args(
      TWorkStatus status)
    {
      this();
      this.status = status;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public reportWorkStatus_args(reportWorkStatus_args other) {
      if (other.isSetStatus()) {
        this.status = new TWorkStatus(other.status);
      }
    }

    public reportWorkStatus_args deepCopy() {
      return new reportWorkStatus_args(this);
    }

    @Override
    public void clear() {
      this.status = null;
    }

    public TWorkStatus getStatus() {
      return this.status;
    }

    public reportWorkStatus_args setStatus(TWorkStatus status) {
      this.status = status;
      return this;
    }

    public void unsetStatus() {
      this.status = null;
    }

    /** Returns true if field status is set (has been assigned a value) and false otherwise */
    public boolean isSetStatus() {
      return this.status != null;
    }

    public void setStatusIsSet(boolean value) {
      if (!value) {
        this.status = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case STATUS:
        if (value == null) {
          unsetStatus();
        } else {
          setStatus((TWorkStatus)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case STATUS:
        return getStatus();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case STATUS:
        return isSetStatus();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof reportWorkStatus_args)
        return this.equals((reportWorkStatus_args)that);
      return false;
    }

    public boolean equals(reportWorkStatus_args that) {
      if (that == null)
        return false;

      boolean this_present_status = true && this.isSetStatus();
      boolean that_present_status = true && that.isSetStatus();
      if (this_present_status || that_present_status) {
        if (!(this_present_status && that_present_status))
          return false;
        if (!this.status.equals(that.status))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(reportWorkStatus_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      reportWorkStatus_args typedOther = (reportWorkStatus_args)other;

      lastComparison = Boolean.valueOf(isSetStatus()).compareTo(typedOther.isSetStatus());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetStatus()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, typedOther.status);
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
      StringBuilder sb = new StringBuilder("reportWorkStatus_args(");
      boolean first = true;

      sb.append("status:");
      if (this.status == null) {
        sb.append("null");
      } else {
        sb.append(this.status);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
      // check for sub-struct validity
      if (status != null) {
        status.validate();
      }
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

    private static class reportWorkStatus_argsStandardSchemeFactory implements SchemeFactory {
      public reportWorkStatus_argsStandardScheme getScheme() {
        return new reportWorkStatus_argsStandardScheme();
      }
    }

    private static class reportWorkStatus_argsStandardScheme extends StandardScheme<reportWorkStatus_args> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, reportWorkStatus_args struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 1: // STATUS
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.status = new TWorkStatus();
                struct.status.read(iprot);
                struct.setStatusIsSet(true);
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

      public void write(org.apache.thrift.protocol.TProtocol oprot, reportWorkStatus_args struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.status != null) {
          oprot.writeFieldBegin(STATUS_FIELD_DESC);
          struct.status.write(oprot);
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class reportWorkStatus_argsTupleSchemeFactory implements SchemeFactory {
      public reportWorkStatus_argsTupleScheme getScheme() {
        return new reportWorkStatus_argsTupleScheme();
      }
    }

    private static class reportWorkStatus_argsTupleScheme extends TupleScheme<reportWorkStatus_args> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, reportWorkStatus_args struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetStatus()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetStatus()) {
          struct.status.write(oprot);
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, reportWorkStatus_args struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          struct.status = new TWorkStatus();
          struct.status.read(iprot);
          struct.setStatusIsSet(true);
        }
      }
    }

  }

}
