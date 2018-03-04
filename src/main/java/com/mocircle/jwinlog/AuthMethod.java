package com.mocircle.jwinlog;

import com.sun.jna.platform.win32.Winevt;

public interface AuthMethod {

	int DEFAULT = Winevt.EVT_RPC_LOGIN_FLAGS.EvtRpcLoginAuthDefault;

	int NEGOTIATE = Winevt.EVT_RPC_LOGIN_FLAGS.EvtRpcLoginAuthNegotiate;

	int KERBEROS = Winevt.EVT_RPC_LOGIN_FLAGS.EvtRpcLoginAuthKerberos;

	int NTLM = Winevt.EVT_RPC_LOGIN_FLAGS.EvtRpcLoginAuthNTLM;

}
