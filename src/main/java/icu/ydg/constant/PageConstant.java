package icu.ydg.constant;

/**
 * 页面常量
 *
 * @author 袁德光
 * @date 2024/07/26
 */
public interface PageConstant {

    /**
     * 当前页码（默认）
     */
   long CURRENT_PAGE = 1;

    /**
     * 页面大小（默认）
     */
    long PAGE_SIZE = 10;

    /**
     * 升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 降序
     */
    String SORT_ORDER_DESC = "descend";

    /**
     * 默认按id排序
     */
    String SORT_BY_ID = "id";
}
