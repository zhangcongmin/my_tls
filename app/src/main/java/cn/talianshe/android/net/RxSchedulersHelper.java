package cn.talianshe.android.net;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RxSchedulersHelper {

    public static <T> Observable.Transformer<T, T> io_main() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}

