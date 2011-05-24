package actorproto

sealed trait Message { def payload: String }

sealed trait LowLevelCollectionMessages extends Message
case class Collect(payload: String) extends LowLevelCollectionMessages
case class CollectRegistry(payload: String) extends LowLevelCollectionMessages
case class CollectFileSystem(payload: String) extends LowLevelCollectionMessages
case class CollectionData(payload: String) extends LowLevelCollectionMessages

sealed trait Remediation extends Message
case class SimpleRemediation(payload: String) extends Remediation
case class ComplexRemediation(payload: String) extends Remediation