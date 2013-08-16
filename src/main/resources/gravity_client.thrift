#!/usr/bin/thrift --java

service TGravityClient {
  void startWork(1:string profile),
  void pauseWork(),
  void resumeWork(),
  void stopWork(),
}

