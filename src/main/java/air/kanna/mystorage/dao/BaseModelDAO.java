package air.kanna.mystorage.dao;

import java.util.List;

public interface BaseModelDAO<T> {
    
    T getById(Object id);
    
    List<T> listAll(OrderBy order, Pager pager);
}