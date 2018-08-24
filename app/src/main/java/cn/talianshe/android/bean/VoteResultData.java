package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VoteResultData extends BaseBean<VoteResultData.VoteResultInfo> {

    public class VoteResultInfo {
        public String voteId;//投票id
        public String voteTitle;//投票id
        @SerializedName("rows")
        public List<VoteResult> voteResults;
    }
    public class VoteResult{
        @SerializedName("sumcount")
        public int totalCount;
        public float avgcount;//投票占百分比
        @SerializedName("autoid")
        public String id;
        public String name;
        @SerializedName("scount")
        public int voteCount;
        @SerializedName("vote_id")
        public String voteId;
    }
}
