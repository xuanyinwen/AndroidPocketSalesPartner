// 
// Created by the DataSnap proxy generator.
// 1/03/2012 2:40:30 p.m.
// 

package com.embarcadero.javaandroid;

import java.util.Date;

public class DSProxy {
  public static class TServerMethods1 extends DSAdmin {
    public TServerMethods1(DSRESTConnection Connection) {
      super(Connection);
    }
    
    
    private DSRESTParameterMetaData[] TServerMethods1_LogIn_Metadata;
    private DSRESTParameterMetaData[] get_TServerMethods1_LogIn_Metadata() {
      if (TServerMethods1_LogIn_Metadata == null) {
        TServerMethods1_LogIn_Metadata = new DSRESTParameterMetaData[]{
          new DSRESTParameterMetaData("UserName", DSRESTParamDirection.Input, DBXDataTypes.WideStringType, "string"),
          new DSRESTParameterMetaData("Password", DSRESTParamDirection.Input, DBXDataTypes.WideStringType, "string"),
          new DSRESTParameterMetaData("ProfileName", DSRESTParamDirection.Input, DBXDataTypes.WideStringType, "string"),
          new DSRESTParameterMetaData("", DSRESTParamDirection.ReturnValue, DBXDataTypes.WideStringType, "string"),
        };
      }
      return TServerMethods1_LogIn_Metadata;
    }

    /**
     * @param UserName [in] - Type on server: string
     * @param Password [in] - Type on server: string
     * @param ProfileName [in] - Type on server: string
     * @return result - Type on server: string
     */
    public String LogIn(String UserName, String Password, String ProfileName) throws DBXException {
      DSRESTCommand cmd = getConnection().CreateCommand();
      cmd.setRequestType(DSHTTPRequestType.GET);
      cmd.setText("TServerMethods1.LogIn");
      cmd.prepare(get_TServerMethods1_LogIn_Metadata());
      cmd.getParameter(0).getValue().SetAsString(UserName);
      cmd.getParameter(1).getValue().SetAsString(Password);
      cmd.getParameter(2).getValue().SetAsString(ProfileName);
      getConnection().execute(cmd);
      return  cmd.getParameter(3).getValue().GetAsString();
    }
    
    
    private DSRESTParameterMetaData[] TServerMethods1_LogOut_Metadata;
    private DSRESTParameterMetaData[] get_TServerMethods1_LogOut_Metadata() {
      if (TServerMethods1_LogOut_Metadata == null) {
        TServerMethods1_LogOut_Metadata = new DSRESTParameterMetaData[]{
        };
      }
      return TServerMethods1_LogOut_Metadata;
    }

    public void LogOut() throws DBXException {
      DSRESTCommand cmd = getConnection().CreateCommand();
      cmd.setRequestType(DSHTTPRequestType.GET);
      cmd.setText("TServerMethods1.LogOut");
      cmd.prepare(get_TServerMethods1_LogOut_Metadata());
      getConnection().execute(cmd);
      return;
    }
    
    
    private DSRESTParameterMetaData[] TServerMethods1_GetListing_Metadata;
    private DSRESTParameterMetaData[] get_TServerMethods1_GetListing_Metadata() {
      if (TServerMethods1_GetListing_Metadata == null) {
        TServerMethods1_GetListing_Metadata = new DSRESTParameterMetaData[]{
          new DSRESTParameterMetaData("Ref", DSRESTParamDirection.Input, DBXDataTypes.WideStringType, "string"),
          new DSRESTParameterMetaData("", DSRESTParamDirection.ReturnValue, DBXDataTypes.JsonValueType, "TListing"),
        };
      }
      return TServerMethods1_GetListing_Metadata;
    }

    /**
     * @param Ref [in] - Type on server: string
     * @return result - Type on server: TListing
     */
    public TJSONObject GetListing(String Ref) throws DBXException {
      DSRESTCommand cmd = getConnection().CreateCommand();
      cmd.setRequestType(DSHTTPRequestType.GET);
      cmd.setText("TServerMethods1.GetListing");
      cmd.prepare(get_TServerMethods1_GetListing_Metadata());
      cmd.getParameter(0).getValue().SetAsString(Ref);
      getConnection().execute(cmd);
      return (TJSONObject) cmd.getParameter(1).getValue().GetAsJSONValue();
    }
    
    
    private DSRESTParameterMetaData[] TServerMethods1_GetListings_Metadata;
    private DSRESTParameterMetaData[] get_TServerMethods1_GetListings_Metadata() {
      if (TServerMethods1_GetListings_Metadata == null) {
        TServerMethods1_GetListings_Metadata = new DSRESTParameterMetaData[]{
          new DSRESTParameterMetaData("StatusList", DSRESTParamDirection.Input, DBXDataTypes.WideStringType, "string"),
          new DSRESTParameterMetaData("", DSRESTParamDirection.ReturnValue, DBXDataTypes.JsonValueType, "TJSONArray"),
        };
      }
      return TServerMethods1_GetListings_Metadata;
    }

    /**
     * @param StatusList [in] - Type on server: string
     * @return result - Type on server: TJSONArray
     */
    public TJSONArray GetListings(String StatusList) throws DBXException {
      DSRESTCommand cmd = getConnection().CreateCommand();
      cmd.setRequestType(DSHTTPRequestType.GET);
      cmd.setText("TServerMethods1.GetListings");
      cmd.prepare(get_TServerMethods1_GetListings_Metadata());
      cmd.getParameter(0).getValue().SetAsString(StatusList);
      getConnection().execute(cmd);
      return (TJSONArray) cmd.getParameter(1).getValue().GetAsJSONValue();
    }
    
    
    private DSRESTParameterMetaData[] TServerMethods1_GetSoldListings_Metadata;
    private DSRESTParameterMetaData[] get_TServerMethods1_GetSoldListings_Metadata() {
      if (TServerMethods1_GetSoldListings_Metadata == null) {
        TServerMethods1_GetSoldListings_Metadata = new DSRESTParameterMetaData[]{
          new DSRESTParameterMetaData("MonthsToGoBack", DSRESTParamDirection.Input, DBXDataTypes.Int32Type, "Integer"),
          new DSRESTParameterMetaData("", DSRESTParamDirection.ReturnValue, DBXDataTypes.JsonValueType, "TJSONArray"),
        };
      }
      return TServerMethods1_GetSoldListings_Metadata;
    }

    /**
     * @param MonthsToGoBack [in] - Type on server: Integer
     * @return result - Type on server: TJSONArray
     */
    public TJSONArray GetSoldListings(int MonthsToGoBack) throws DBXException {
      DSRESTCommand cmd = getConnection().CreateCommand();
      cmd.setRequestType(DSHTTPRequestType.GET);
      cmd.setText("TServerMethods1.GetSoldListings");
      cmd.prepare(get_TServerMethods1_GetSoldListings_Metadata());
      cmd.getParameter(0).getValue().SetAsInt32(MonthsToGoBack);
      getConnection().execute(cmd);
      return (TJSONArray) cmd.getParameter(1).getValue().GetAsJSONValue();
    }
    
    
    private DSRESTParameterMetaData[] TServerMethods1_GetPicture_Metadata;
    private DSRESTParameterMetaData[] get_TServerMethods1_GetPicture_Metadata() {
      if (TServerMethods1_GetPicture_Metadata == null) {
        TServerMethods1_GetPicture_Metadata = new DSRESTParameterMetaData[]{
          new DSRESTParameterMetaData("Ref", DSRESTParamDirection.Input, DBXDataTypes.WideStringType, "string"),
          new DSRESTParameterMetaData("num", DSRESTParamDirection.Input, DBXDataTypes.Int32Type, "Integer"),
          new DSRESTParameterMetaData("width", DSRESTParamDirection.Input, DBXDataTypes.Int32Type, "Integer"),
          new DSRESTParameterMetaData("height", DSRESTParamDirection.Input, DBXDataTypes.Int32Type, "Integer"),
          new DSRESTParameterMetaData("", DSRESTParamDirection.ReturnValue, DBXDataTypes.BinaryBlobType, "TStream"),
        };
      }
      return TServerMethods1_GetPicture_Metadata;
    }

    /**
     * @param Ref [in] - Type on server: string
     * @param num [in] - Type on server: Integer
     * @param width [in] - Type on server: Integer
     * @param height [in] - Type on server: Integer
     * @return result - Type on server: TStream
     */
    public TStream GetPicture(String Ref, int num, int width, int height) throws DBXException {
      DSRESTCommand cmd = getConnection().CreateCommand();
      cmd.setRequestType(DSHTTPRequestType.GET);
      cmd.setText("TServerMethods1.GetPicture");
      cmd.prepare(get_TServerMethods1_GetPicture_Metadata());
      cmd.getParameter(0).getValue().SetAsString(Ref);
      cmd.getParameter(1).getValue().SetAsInt32(num);
      cmd.getParameter(2).getValue().SetAsInt32(width);
      cmd.getParameter(3).getValue().SetAsInt32(height);
      getConnection().execute(cmd);
      return  cmd.getParameter(4).getValue().GetAsStream();
    }
    
    
    private DSRESTParameterMetaData[] TServerMethods1_GetMediaList_Metadata;
    private DSRESTParameterMetaData[] get_TServerMethods1_GetMediaList_Metadata() {
      if (TServerMethods1_GetMediaList_Metadata == null) {
        TServerMethods1_GetMediaList_Metadata = new DSRESTParameterMetaData[]{
          new DSRESTParameterMetaData("Ref", DSRESTParamDirection.Input, DBXDataTypes.WideStringType, "string"),
          new DSRESTParameterMetaData("", DSRESTParamDirection.ReturnValue, DBXDataTypes.JsonValueType, "TObjectList<ServerMethodsUnit1.TFileDetails>"),
        };
      }
      return TServerMethods1_GetMediaList_Metadata;
    }

    /**
     * @param Ref [in] - Type on server: string
     * @return result - Type on server: TObjectList<ServerMethodsUnit1.TFileDetails>
     */
    public TJSONObject GetMediaList(String Ref) throws DBXException {
      DSRESTCommand cmd = getConnection().CreateCommand();
      cmd.setRequestType(DSHTTPRequestType.GET);
      cmd.setText("TServerMethods1.GetMediaList");
      cmd.prepare(get_TServerMethods1_GetMediaList_Metadata());
      cmd.getParameter(0).getValue().SetAsString(Ref);
      getConnection().execute(cmd);
      return (TJSONObject) cmd.getParameter(1).getValue().GetAsJSONValue();
    }
    
    
    private DSRESTParameterMetaData[] TServerMethods1_GetMediaFile_Metadata;
    private DSRESTParameterMetaData[] get_TServerMethods1_GetMediaFile_Metadata() {
      if (TServerMethods1_GetMediaFile_Metadata == null) {
        TServerMethods1_GetMediaFile_Metadata = new DSRESTParameterMetaData[]{
          new DSRESTParameterMetaData("Ref", DSRESTParamDirection.Input, DBXDataTypes.WideStringType, "string"),
          new DSRESTParameterMetaData("FileName", DSRESTParamDirection.Input, DBXDataTypes.WideStringType, "string"),
          new DSRESTParameterMetaData("", DSRESTParamDirection.ReturnValue, DBXDataTypes.BinaryBlobType, "TStream"),
        };
      }
      return TServerMethods1_GetMediaFile_Metadata;
    }

    /**
     * @param Ref [in] - Type on server: string
     * @param FileName [in] - Type on server: string
     * @return result - Type on server: TStream
     */
    public TStream GetMediaFile(String Ref, String FileName) throws DBXException {
      DSRESTCommand cmd = getConnection().CreateCommand();
      cmd.setRequestType(DSHTTPRequestType.GET);
      cmd.setText("TServerMethods1.GetMediaFile");
      cmd.prepare(get_TServerMethods1_GetMediaFile_Metadata());
      cmd.getParameter(0).getValue().SetAsString(Ref);
      cmd.getParameter(1).getValue().SetAsString(FileName);
      getConnection().execute(cmd);
      return  cmd.getParameter(2).getValue().GetAsStream();
    }
    
    
    private DSRESTParameterMetaData[] TServerMethods1_AuthenticationRequired_Metadata;
    private DSRESTParameterMetaData[] get_TServerMethods1_AuthenticationRequired_Metadata() {
      if (TServerMethods1_AuthenticationRequired_Metadata == null) {
        TServerMethods1_AuthenticationRequired_Metadata = new DSRESTParameterMetaData[]{
          new DSRESTParameterMetaData("", DSRESTParamDirection.ReturnValue, DBXDataTypes.BooleanType, "Boolean"),
        };
      }
      return TServerMethods1_AuthenticationRequired_Metadata;
    }

    /**
     * @return result - Type on server: Boolean
     */
    public boolean AuthenticationRequired() throws DBXException {
      DSRESTCommand cmd = getConnection().CreateCommand();
      cmd.setRequestType(DSHTTPRequestType.GET);
      cmd.setText("TServerMethods1.AuthenticationRequired");
      cmd.prepare(get_TServerMethods1_AuthenticationRequired_Metadata());
      getConnection().execute(cmd);
      return  cmd.getParameter(0).getValue().GetAsBoolean();
    }
  }

}
