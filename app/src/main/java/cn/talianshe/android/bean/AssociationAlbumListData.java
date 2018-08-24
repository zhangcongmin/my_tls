package cn.talianshe.android.bean;


public class AssociationAlbumListData extends BaseListBean<AssociationAlbumListData.AssociationAlbumListInfo> {
    public class AssociationAlbumListInfo extends BaseListData<AssociationAlbum> {}

    public class AssociationAlbum {
        /**
         * id : 2
         * cover :
         * counts : 0
         * activityName : 活动2
         */

        public int id;
        public String cover;
        public String counts;
        public String activityName;
    }
}
