package leave.meet.playbours.manage.sys.menu.repository.impl;

import leave.meet.playbours.manage.sys.menu.repository.MaMenuRepository;
import leave.meet.playbours.manage.sys.menu.service.MaMenuDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class MaMenuRepositoryImpl implements MaMenuRepository {

    private final MongoTemplate mongoTemplate;

    public MaMenuRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<MaMenuDto> findByPagingAndFiltering(int page, int size, MaMenuDto maMenuDto, String procType) {
        Pageable pageable = PageRequest.of(page == 0 ? 0 : page - 1, size, Sort.by("seq"));
        Query query = new Query()
                .with(pageable)
                .skip((long) pageable.getPageSize() * pageable.getPageNumber())
                .limit(pageable.getPageSize());

        query.addCriteria(Criteria.where("delYn").is("N"));

        /* 쿼리 조건 */
        if(maMenuDto.getSearch1() != null && !"".equals(maMenuDto.getSearch1() )) {
            query.addCriteria(Criteria.where("menuClCd").is(maMenuDto.getSearch1()));
        }

        if("list".equals(procType)) {
            if(maMenuDto.getSearchKeyword() != null && !"".equals(maMenuDto.getSearchKeyword())) {
                if(maMenuDto.getSearchOption() != null && !"".equals(maMenuDto.getSearchOption())) {
                    switch (maMenuDto.getSearchOption()) {
                        case "0" -> {
                            Criteria criteria = new Criteria();
                            criteria.orOperator(Criteria.where("menuCd").is(maMenuDto.getSearchKeyword()), Criteria.where("menuNm").is(maMenuDto.getSearchKeyword()));
                            query.addCriteria(criteria);
                        }
                        case "1" -> {
                            query.addCriteria(Criteria.where("menuNm").is(maMenuDto.getSearchKeyword()));
                        }
                        case "2" -> {
                            query.addCriteria(Criteria.where("menuCd").is(maMenuDto.getSearchKeyword()));
                        }
                    }
                }
            }
        }

        query.addCriteria(Criteria.where("upperCd").is("view".equals(procType) ? maMenuDto.getMenuCd() : ""));
        query.with(Sort.by(Sort.Direction.DESC, "seq"));

        List<MaMenuDto> filterData = mongoTemplate.find(query, MaMenuDto.class);

        return PageableExecutionUtils.getPage(
                filterData, pageable, () -> mongoTemplate.count(query.skip(-1).limit(-1), MaMenuDto.class)
        );
    }

    @Override
    public MaMenuDto findOne(MaMenuDto maMenuDto) {
        Query query = new Query();
        query.addCriteria(Criteria.where("seq").is(maMenuDto.getSeq()));
        query.addCriteria(Criteria.where("delYn").is("N"));
        return mongoTemplate.findOne(query, MaMenuDto.class);
    }

    @Override
    public MaMenuDto findOneByCode(MaMenuDto maMenuDto) {
        Query query = new Query();
        query.addCriteria(Criteria.where("menuCd").is(maMenuDto.getUpperCd()));
        query.addCriteria(Criteria.where("delYn").is("N"));
        return mongoTemplate.findOne(query, MaMenuDto.class);
    }

    @Override
    public int countByCode(String menuCd) {
        Query query = new Query();
        query.addCriteria(Criteria.where("menuCd").is(menuCd));
        query.addCriteria(Criteria.where("delYn").is("N"));
        return (int) mongoTemplate.count(query, MaMenuDto.class);
    }

    @Override
    public void insert(MaMenuDto dto) {
        MaMenuDto menuDto = new MaMenuDto();
        menuDto.setUpperCd(dto.getUpperCd());
        menuDto.setMenuClCd(dto.getMenuClCd());
        menuDto.setMenuOrd(dto.getMenuOrd());
        menuDto.setUseYn(dto.getUseYn());
        menuDto.setMenuNm(dto.getMenuNm());
        menuDto.setMenuCd(dto.getMenuCd());
        menuDto.setMenuUrl(dto.getMenuUrl());
        menuDto.setMenuCmt(dto.getMenuCmt());
        // TODO 로그인한 아이디로 변경
        menuDto.setFrstRegrId("admin");
        menuDto.setFrstRegrDt(new Date());
        menuDto.setDelYn("N");
        mongoTemplate.insert(menuDto);
    }

    @Override
    public void update(MaMenuDto dto) {
        Query query = new Query();
        Update update = new Update();

        query.addCriteria(Criteria.where("seq").is(dto.getSeq()));
        update.set("upperCd", dto.getUpperCd());
        update.set("menuClCd", dto.getMenuClCd());
        update.set("menuOrd", dto.getMenuOrd());
        update.set("useYn", dto.getUseYn());
        update.set("menuNm", dto.getMenuNm());
        update.set("menuCd", dto.getMenuCd());
        update.set("menuUrl", dto.getMenuUrl());
        update.set("menuCmt", dto.getMenuCmt());
        // TODO 로그인한 아이디로 변경
        update.set("lstChgId", "admin");
        update.set("lstChgDt", new Date());
        mongoTemplate.upsert(query, update, MaMenuDto.class);
    }

    @Override
    public void delete(MaMenuDto dto) {
        Query query = new Query();
        Update update = new Update();

        // TODO 로그인한 아이디로 변경
        update.set("lstChgId", "admin");
        update.set("lstChgDt", new Date());
        update.set("delYn", "Y");
        mongoTemplate.upsert(query, update, MaMenuDto.class);
    }
}
