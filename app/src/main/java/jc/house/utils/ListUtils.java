package jc.house.utils;

import java.util.List;

/**
 * Created by hzj on 2016/1/30.
 */
public class ListUtils {
    public static boolean isValid(List<? extends Object> list) {
        return (null == list || list.isEmpty());
    }
}
