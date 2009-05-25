package org.isam.exehda.services.worb;

abstract interface WorbProtocolConstants
{
  public static final String WORB_REQ_PROP_RESOURCE = "Resource";
  public static final String WORB_REQ_PROP_OPNUM = "Isam-Worb-Opnum";
  public static final String WORB_REQ_PROP_PROTO_VERSION = "X-Worb-Version";
  public static final String WORB_REQ_PROP_OP_HASH = "X-Worb-Op-Hash";
  public static final String WORB_REQ_PROP_OP_SESSION = "X-Worb-Op-Session";
  public static final String WORB_REQ_PROP_OP_RESUME = "X-Worb-Op-Resume";
}