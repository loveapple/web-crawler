-- 修正字段名和索引
ALTER TABLE site_contents CHANGE site_categoy_id site_category_id int(10) NOT NULL;
CREATE UNIQUE INDEX idx_url_full ON site_contents(url(1000)); 