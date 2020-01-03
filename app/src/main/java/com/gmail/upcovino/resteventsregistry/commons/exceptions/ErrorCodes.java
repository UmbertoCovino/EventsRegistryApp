package com.gmail.upcovino.resteventsregistry.commons.exceptions;

public class ErrorCodes {
	public static final int INVALID_EVENT_ID = 800,
			                INVALID_USER_EMAIL = 900,
			                UNAUTHORIZED_USER = 901,
			                VOID_CLASS_FIELD = 950,
					        GENERIC_SQL = 951,
					        JSON_PARSING = 952,
					        DATE_PARSING = 953,
					        URI_ENCODING = 954,
					        RESOURCE_EXCEPTION = 955,
					        INVALID_PARTICIPATION = 956,
					        INVALID_TOKEN = 957;
	
	public static final String INVALID_EVENT_ID_DESCRIPTION = "Invalid Event Id",
							  INVALID_USER_EMAIL_DESCRIPTION = "Invalid User Email",
							  UNAUTHORIZED_USER_DESCRIPTION = "Unauthorized User",
				              VOID_CLASS_FIELD_EXC_DESCRIPTION = "Void Class Field(s)",
						      GENERIC_SQL_EXC_DESCRIPTION = "Generic SQL Error",
						      JSON_PARSING_EXC_DESCRIPTION = "Json Parsing Error",
							  DATE_PARSING_EXC_DESCRIPTION = "Date Parsing Error",
							  URI_ENCODING_EXC_DESCRIPTION = "Uri Encoding Error",
							  RESOURCE_EXC_DESCRIPTION = "Resource Error",
							  INVALID_PARTICIPATION_DESCRIPTION = "Invalid Participation",
							  INVALID_TOKEN_DESCRIPTION = "Invalid Token";
}
