package com.nextify.blog.utils;

import com.nextify.blog.dto.PermissionTreeDto;
import com.nextify.blog.entity.SysPermission;
import com.nextify.blog.vo.PermissionTreeVO;
import org.lionsoul.ip2region.xdb.LittleEndian;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionVOConverter {

    private PermissionVOConverter(){}

    public static List<PermissionTreeVO> toTree(List<SysPermission> all, Long startParentId){
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }
        List<PermissionTreeVO> allVO = all.stream()
            .map(PermissionVOConverter::toVO)
            .toList();
        // 按照parentId 分组
        Map<Long, List<PermissionTreeVO>> parentMap = allVO.stream()
            .collect(Collectors.groupingBy(
                item -> item.getParentId() != null ? item.getParentId() : 0L
            ));
        Long start = (startParentId != null && startParentId > 0) ? startParentId : 0L;
        // 迭代挂载子节点
        for(PermissionTreeVO vo : allVO) {
            List<PermissionTreeVO> children = parentMap.get(vo.getId());
            if(!CollectionUtils.isEmpty(children)) {
                vo.setChildren(children);
            }
        }

        return parentMap.getOrDefault(start, Collections.emptyList());

    }

    /**
     * 单条实体 → VO（扁平转换，不含 children）
     */
    public static PermissionTreeVO toVO(SysPermission entity) {
        if (entity == null) return null;
        PermissionTreeVO vo = new PermissionTreeVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setPermCode(entity.getPermCode());
        vo.setPermName(entity.getPermName());
        vo.setPermType(entity.getPermType());
        vo.setPath(entity.getPath());
        vo.setComponent(entity.getComponent());
        vo.setIcon(entity.getIcon());
        vo.setSort(entity.getSort());
        vo.setVisible(entity.getVisible());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 批量实体 → VO 列表（扁平，不含 children）
     */
    public static List<PermissionTreeVO> toVOList(List<SysPermission> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
            .map(PermissionVOConverter::toVO)
            .collect(Collectors.toList());
    }

}
