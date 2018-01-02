package com.tangsoft.xkr.jiujiaotianxia.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 *
 */

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final int RETURN_MSG_TYPE_LOGIN = 1;
    private static final int RETURN_MSG_TYPE_SHARE = 2;
    private String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    private IWXAPI mWxAPI;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWxAPI = WXAPIFactory.createWXAPI(this, WXPayEntryActivity.WX_APPID);
        mWxAPI.handleIntent(getIntent(),this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.i("my",baseResp.errStr);
        Log.i("my","9������ : " + baseResp.errCode + "");
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                if (RETURN_MSG_TYPE_SHARE == baseResp.getType()) {
                    Toast.makeText(WXEntryActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Toast.makeText(WXEntryActivity.this, "��¼ʧ��", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case BaseResp.ErrCode.ERR_OK:
                switch (baseResp.getType()) {
                    case RETURN_MSG_TYPE_LOGIN:
                        //�õ���΢�ŷ��ص�code,��ȥ����access_token
                        String code = ((SendAuth.Resp) baseResp).code;
                        Log.i("my","code = " + code);
//                        requestWXToken(code);
//                        requestUserInfo(code);
                        break;

                    case RETURN_MSG_TYPE_SHARE:
                        Toast.makeText(WXEntryActivity.this, "΢�ŷ����ɹ�", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    default:
                        finish();
                }
                break;
            default:
                finish();
        }

    }

//    public void requestWXToken(String code){
//        Map<String,Object> map = new HashMap<>();
//        Http.get("https://api.weixin.qq.com/sns/oauth2/access_token?" +
//                "appid="+ Config.WX_AppID +
//                "&secret="+ Config.WX_AppSecret +
//                "&code="+ code +
//                "&grant_type=authorization_code",map,new JsonRespHandler(){
//
//            @Override
//            public boolean onMatchAppStatusCode(ReqInfo reqInfo, RespInfo respInfo, JsonModel resultBean) {
//                return true;
//            }
//
//            @Override
//            public void onSuccessAll(ReqInfo reqInfo, RespInfo respInfo, JsonModel resultBean) {
//                super.onSuccessAll(reqInfo, respInfo, resultBean);
//                if(!TextUtils.isEmpty(resultBean.getString("access_token"))) {
//                    //requestUserInfo(resultBean.getString("access_token"));
//                    Log.shortToast("΢�ŵ�¼�ɹ� ��" + resultBean.getString("access_token"));
//                }else {
//                    Log.shortToast("΢�ŵ�¼������ ��" + resultBean.getString("errcode"));
//                    finish();
//                }
//
//            }
//        });
//    }
//
//    // ����΢�ŵ�¼�û��ӿ�
//    public void requestUserInfo(String code){
//        try{
//            JSONObject jsonObject = new JSONObject();
//
//            jsonObject.put("code",code);
//
//            Http.postJson(Config.getHostUrl(Config.WECHAT_LOGIN),jsonObject.toString(),new JsonRespHandler(this){
//
//                @Override
//                public boolean onMatchAppStatusCode(ReqInfo reqInfo, RespInfo respInfo, JsonModel resultBean) {
//                    return true;
//                }
//
//                @Override
//                public void onSuccessAll(ReqInfo reqInfo, RespInfo respInfo, JsonModel resultBean) {
//                    super.onSuccessAll(reqInfo, respInfo, resultBean);
//
//                    Sp.setUserToken(resultBean.getString("token"));
//                    Sp.setLogin(true);
//                    if("1".equals(resultBean.getRet())) {
//                        MainActivity.actionStart(WXEntryActivity.this);
//                    }else if("2".equals(resultBean.getRet())){
//                        WebViewActivity.actionStart(WXEntryActivity.this, Config.getHtmlUrl(Config.PASSPORT),"");
//                    }else{
////                        LoginActivity.actionStart(WXEntryActivity.this);
//                        Log.shortToast("��¼ʧ��!");
//                    }
//                    finish();
//                }
//            });
//        }catch (Exception e){e.printStackTrace();}
//    }

}
