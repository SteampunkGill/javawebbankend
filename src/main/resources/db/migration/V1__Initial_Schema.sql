-- File: milktea-backend/src/main/resources/db/migration/V1__Initial_Schema.sql
-- 用户表
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       wechat_openid VARCHAR(64) UNIQUE, -- 微信OpenID，用于微信登录
                       username VARCHAR(50) UNIQUE,     -- 用户名，可空，如果允许纯微信登录
                       password_hash VARCHAR(255),       -- 密码哈希，用于账号密码登录
                       phone VARCHAR(20) UNIQUE,         -- 手机号，可空，可能后续绑定或更新
                       email VARCHAR(100) UNIQUE,        -- 邮箱
                       nickname VARCHAR(50) NOT NULL,    -- 昵称
                       avatar_url VARCHAR(255),          -- 头像URL
                       gender SMALLINT DEFAULT 0,        -- 性别: 0:未知, 1:男, 2:女
                       country VARCHAR(50),              -- 国家
                       province VARCHAR(50),             -- 省份
                       city VARCHAR(50),                 -- 城市
                       birthday DATE,                    -- 生日
                       member_level_id BIGINT,           -- FK to member_levels表
                       growth_value INT DEFAULT 0,       -- 成长值
                       points INT DEFAULT 0,             -- 当前积分
                       balance DECIMAL(10, 2) DEFAULT 0.00, -- 余额
                       member_card_no VARCHAR(50) UNIQUE, -- 会员卡号
                       member_card_status VARCHAR(20) DEFAULT 'inactive', -- active:激活, expired:过期, inactive:未激活
                       member_card_expire_date DATE,     -- 会员卡过期日期
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP WITH TIME ZONE, -- 最后登录时间
                       is_active BOOLEAN DEFAULT TRUE    -- 账号是否活跃/启用
);

CREATE INDEX idx_users_phone ON users (phone);
CREATE INDEX idx_users_wechat_openid ON users (wechat_openid);

-- 会员等级表
CREATE TABLE member_levels (
                               id BIGSERIAL PRIMARY KEY,
                               name VARCHAR(50) NOT NULL UNIQUE, -- 等级名称 (如: 普通, 黄金, 钻石)
                               min_growth_value INT NOT NULL,    -- 达到该等级所需的最小成长值
                               description TEXT,                 -- 等级描述
                               privileges_json JSONB,            -- 存储特权描述的JSON
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 用户地址表
CREATE TABLE user_addresses (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                name VARCHAR(100) NOT NULL,       -- 收货人姓名
                                phone VARCHAR(20) NOT NULL,       -- 手机号
                                province VARCHAR(50) NOT NULL,    -- 省
                                city VARCHAR(50) NOT NULL,        -- 市
                                district VARCHAR(50),             -- 区
                                detail VARCHAR(255) NOT NULL,     -- 详细地址
                                postal_code VARCHAR(10),          -- 邮政编码
                                is_default BOOLEAN DEFAULT FALSE, -- 是否默认地址
                                type VARCHAR(20) DEFAULT 'home',  -- 地址类型 (home, company, school, other)
                                label VARCHAR(50),                -- 地址标签
                                longitude DECIMAL(10, 7),         -- 经度
                                latitude DECIMAL(10, 7),          -- 纬度
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_addresses_user_id ON user_addresses (user_id);
-- CREATE INDEX idx_user_addresses_location ON user_addresses USING GIST (LL_TO_EARTH(latitude, longitude)); -- 假设使用PostGIS或类似扩展

-- 用户分享记录表
CREATE TABLE user_shares (
                             id BIGSERIAL PRIMARY KEY,
                             user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             type VARCHAR(20) NOT NULL,        -- 分享类型 (product, activity, invite)
                             target_id VARCHAR(50) NOT NULL,   -- 目标ID (商品ID, 活动ID, 被邀请用户ID)
                             channel VARCHAR(20) NOT NULL,     -- 渠道 (wechat, moments, qq)
                             invite_code VARCHAR(50),          -- 邀请码 (如果是邀请类型)
                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_shares_user_id ON user_shares (user_id);
CREATE INDEX idx_user_shares_target_id ON user_shares (target_id);

-- 首页轮播图表
CREATE TABLE banners (
                         id BIGSERIAL PRIMARY KEY,
                         image_url VARCHAR(255) NOT NULL,  -- 图片URL
                         title VARCHAR(100),               -- 标题
                         subtitle VARCHAR(100),            -- 副标题
                         type VARCHAR(20) NOT NULL,        -- 跳转类型 (product, activity, url)
                         target_id VARCHAR(50),            -- 目标ID (根据type关联不同表)
                         url VARCHAR(255),                 -- 跳转链接 (如果type是url)
                         background_color VARCHAR(20),     -- 背景颜色
                         sort_order INT DEFAULT 0,         -- 排序
                         is_active BOOLEAN DEFAULT TRUE,   -- 是否启用
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 首页快捷入口表
CREATE TABLE quick_entries (
                               id BIGSERIAL PRIMARY KEY,
                               icon_url VARCHAR(255) NOT NULL,   -- 图标URL
                               name VARCHAR(50) NOT NULL,        -- 名称
                               type VARCHAR(20) NOT NULL,        -- 类型 (category, url)
                               target_id BIGINT, -- REFERENCES categories(id) ON DELETE SET NULL, -- 目标ID (category_id) - 暂时不加外键，因为可以为URL
                               url VARCHAR(255),                 -- 跳转链接 (如果type是url)
                               badge VARCHAR(20),                -- 徽章 (hot, new, null)
                               sort_order INT DEFAULT 0,         -- 排序
                               is_active BOOLEAN DEFAULT TRUE,   -- 是否启用
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 首页促销活动表
CREATE TABLE promotions (
                            id BIGSERIAL PRIMARY KEY,
                            title VARCHAR(100) NOT NULL,      -- 标题
                            subtitle VARCHAR(255),            -- 副标题
                            image_url VARCHAR(255),           -- 背景图片URL
                            type VARCHAR(20) NOT NULL,        -- 类型 (discount:折扣, coupon:优惠券, points:积分)
                            value DECIMAL(10, 2),             -- 优惠值
                            start_time TIMESTAMP WITH TIME ZONE NOT NULL,
                            end_time TIMESTAMP WITH TIME ZONE NOT NULL,
                            button_text VARCHAR(50),          -- 按钮文本
                            is_active BOOLEAN DEFAULT TRUE,   -- 是否启用
                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 商品分类表
CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL, -- 父级ID，用于构建分类树
                            name VARCHAR(50) NOT NULL,        -- 分类名称
                            icon_url VARCHAR(255),            -- 图标URL
                            image_url VARCHAR(255),           -- 分类图片URL
                            description TEXT,                 -- 分类描述
                            sort_order INT DEFAULT 0,         -- 排序值
                            is_active BOOLEAN DEFAULT TRUE,   -- 是否启用
    -- product_count 可通过SQL统计或在应用层缓存
                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL, -- FK to categories
                          name VARCHAR(100) NOT NULL,       -- 商品名称
                          subtitle VARCHAR(255),            -- 副标题
                          main_image_url VARCHAR(255),      -- 主图URL
                          description TEXT,                 -- 商品描述
                          detail_html TEXT,                 -- 详细HTML描述
                          price DECIMAL(10, 2) NOT NULL,    -- 价格
                          original_price DECIMAL(10, 2),    -- 原价
                          unit VARCHAR(20) DEFAULT '杯',    -- 单位
                          stock INT DEFAULT 0,              -- 库存
                          sales INT DEFAULT 0,              -- 总销量
                          monthly_sales INT DEFAULT 0,      -- 月销量
                          rating DECIMAL(2, 1) DEFAULT 0.0, -- 评分
                          rating_count INT DEFAULT 0,       -- 评价数量
                          favorite_count INT DEFAULT 0,     -- 收藏人数
                          is_hot BOOLEAN DEFAULT FALSE,     -- 是否热销
                          is_new BOOLEAN DEFAULT FALSE,     -- 是否新品
                          is_recommend BOOLEAN DEFAULT FALSE, -- 是否推荐
                          tags JSONB,                       -- 标签 (如: ["招牌", "人气"])
                          storage_method VARCHAR(100),      -- 存储方式 (冷藏)
                          shelf_life VARCHAR(50),           -- 保质期 (24 小时)
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          is_active BOOLEAN DEFAULT TRUE    -- 是否上架
);

-- 商品图片表 (一对多关系)
CREATE TABLE product_images (
                                id BIGSERIAL PRIMARY KEY,
                                product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                image_url VARCHAR(255) NOT NULL,
                                sort_order INT DEFAULT 0,
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id);

-- 商品定制类型表 (甜度、温度、加料等)
CREATE TABLE product_customization_types (
                                             id BIGSERIAL PRIMARY KEY,
                                             product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                             type_name VARCHAR(50) NOT NULL,   -- 类型名称 (sweetness, temperature, toppings)
                                             label VARCHAR(50),                -- 显示标签 (甜度, 温度, 加料)
                                             is_enabled BOOLEAN DEFAULT TRUE,  -- 是否启用此定制类型
                                             is_required BOOLEAN DEFAULT FALSE, -- 是否必选
                                             max_quantity INT,                 -- 最大数量 (只对toppings有意义)
                                             sort_order INT DEFAULT 0,
                                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_customization_types_product_id ON product_customization_types (product_id);

-- 商品定制选项表 (无糖、去冰、珍珠等)
CREATE TABLE product_customization_options (
                                               id BIGSERIAL PRIMARY KEY,
                                               customization_type_id BIGINT NOT NULL REFERENCES product_customization_types(id) ON DELETE CASCADE,
                                               value VARCHAR(50) NOT NULL,       -- 选项值 (no_sugar, no_ice, topping_1)
                                               label VARCHAR(50) NOT NULL,       -- 选项显示名称 (无糖, 去冰, 珍珠)
                                               price_adjustment DECIMAL(10, 2) DEFAULT 0.00, -- 价格调整
                                               is_default BOOLEAN DEFAULT FALSE, -- 是否默认选项
                                               stock INT,                        -- 库存 (对于加料项，如珍珠)
                                               icon_url VARCHAR(255),            -- 图标URL (对于加料)
                                               sort_order INT DEFAULT 0,
                                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_customization_options_type_id ON product_customization_options (customization_type_id);

-- 商品营养成分表
CREATE TABLE product_nutritions (
                                    id BIGSERIAL PRIMARY KEY,
                                    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                    name VARCHAR(50) NOT NULL,        -- 成分名称 (热量, 碳水化合物, 蛋白质)
                                    value VARCHAR(50) NOT NULL,       -- 值 (250 千卡, 45g)
                                    unit VARCHAR(20),                 -- 单位 (千卡, g)
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_nutritions_product_id ON product_nutritions (product_id);

-- 原料主表
CREATE TABLE ingredients (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(50) NOT NULL UNIQUE,
                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 商品-原料关联表 (多对多)
CREATE TABLE product_ingredients_map (
                                         product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                         ingredient_id BIGINT NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE,
                                         PRIMARY KEY (product_id, ingredient_id)
);

-- 过敏原主表
CREATE TABLE allergens (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(50) NOT NULL UNIQUE,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 商品-过敏原关联表 (多对多)
CREATE TABLE product_allergens_map (
                                       product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                       allergen_id BIGINT NOT NULL REFERENCES allergens(id) ON DELETE CASCADE,
                                       PRIMARY KEY (product_id, allergen_id)
);

-- 相关商品关联表 (自关联，多对多)
CREATE TABLE product_related_map (
                                     main_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                     related_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                     PRIMARY KEY (main_product_id, related_product_id),
                                     CONSTRAINT chk_different_products CHECK (main_product_id != related_product_id) -- 确保不是自己关联自己
    );

-- 搜索关键词表 (用于热词、建议)
CREATE TABLE search_keywords (
                                 id BIGSERIAL PRIMARY KEY,
                                 keyword VARCHAR(100) NOT NULL UNIQUE, -- 关键词
                                 count INT DEFAULT 0,              -- 搜索次数
                                 type VARCHAR(20),                 -- 类型 (hot, new)
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 用户搜索历史表
CREATE TABLE user_search_histories (
                                       id BIGSERIAL PRIMARY KEY,
                                       user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       keyword VARCHAR(100) NOT NULL,    -- 搜索词
                                       type VARCHAR(20),                 -- 类型 (product, category)
                                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_search_histories_user_id ON user_search_histories (user_id);
CREATE INDEX idx_user_search_histories_keyword ON user_search_histories (keyword);

-- 用户收藏商品表 (多对多)
CREATE TABLE user_favorite_products (
                                        id BIGSERIAL PRIMARY KEY, -- 添加ID作为主键以支持favoriteId返回
                                        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        UNIQUE (user_id, product_id) -- 每个用户同一种商品只能收藏一次
);

CREATE INDEX idx_user_favorite_products_user_id ON user_favorite_products (user_id);

-- 购物车项表 (用户-商品-定制项的组合)
CREATE TABLE cart_items (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                            quantity INT NOT NULL DEFAULT 1,
                            is_selected BOOLEAN DEFAULT TRUE, -- 是否选中结算
                            is_valid BOOLEAN DEFAULT TRUE,    -- 是否有效 (如库存是否足够，商品是否下架等)
                            invalid_reason VARCHAR(255),      -- 无效原因
                            price_at_add DECIMAL(10, 2) NOT NULL, -- 添加时商品价格 (用于价格快照)
                            original_price_at_add DECIMAL(10, 2), -- 添加时商品原价
                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE (user_id, product_id) -- 每个用户同一种商品（不区分定制项）只能有一条购物车记录
);

CREATE INDEX idx_cart_items_user_id ON cart_items (user_id);

-- 购物车项定制选项表 (与购物车项一对多)
CREATE TABLE cart_item_customizations (
                                          id BIGSERIAL PRIMARY KEY,
                                          cart_item_id BIGINT NOT NULL REFERENCES cart_items(id) ON DELETE CASCADE,
                                          customization_type_name VARCHAR(50) NOT NULL, -- 定制类型名称 (sweetness, temperature, toppings)
                                          option_value VARCHAR(50) NOT NULL,            -- 选项值 (50_percent, no_ice, topping_1)
                                          option_label VARCHAR(50) NOT NULL,            -- 选项显示名称
                                          price_adjustment_at_add DECIMAL(10, 2) DEFAULT 0.00, -- 添加时价格调整
                                          quantity INT DEFAULT 1,                       -- 数量 (对于加料，如珍珠可以加多份)
                                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cart_item_customizations_cart_item_id ON cart_item_customizations (cart_item_id);

-- 门店表
CREATE TABLE stores (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,       -- 门店名称
                        address VARCHAR(255) NOT NULL,    -- 详细地址
                        phone VARCHAR(20),                -- 联系电话
                        business_hours VARCHAR(100),      -- 营业时间 (如 "9:00-22:00")
                        status VARCHAR(20) DEFAULT 'open', -- 状态 (open:营业中, closed:已打烊, busy:繁忙)
                        longitude DECIMAL(10, 7) NOT NULL,
                        latitude DECIMAL(10, 7) NOT NULL,
                        delivery_fee DECIMAL(10, 2) DEFAULT 0.00, -- 配送费
                        minimum_order_amount DECIMAL(10, 2) DEFAULT 0.00, -- 起送价
                        rating DECIMAL(2, 1) DEFAULT 0.0, -- 评分
                        current_wait_time INT DEFAULT 0,  -- 当前等待时间(分钟)
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        is_active BOOLEAN DEFAULT TRUE    -- 门店是否启用
);

-- CREATE INDEX idx_stores_location ON stores USING GIST (LL_TO_EARTH(latitude, longitude));

-- 门店服务表 (配送、自取等，多对多)
CREATE TABLE store_services (
                                store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
                                service_type VARCHAR(20) NOT NULL, -- 服务类型 (delivery, pickup)
                                PRIMARY KEY (store_id, service_type)
);

-- 门店标签表 (人气门店、支持自取等，多对多)
CREATE TABLE store_tags (
                            store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
                            tag_name VARCHAR(50) NOT NULL,    -- 标签名称 (人气门店, 支持自取)
                            PRIMARY KEY (store_id, tag_name)
);

-- 门店图片表
CREATE TABLE store_images (
                              id BIGSERIAL PRIMARY KEY,
                              store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
                              image_url VARCHAR(255) NOT NULL,
                              sort_order INT DEFAULT 0,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 用户收藏门店表 (多对多)
CREATE TABLE user_favorite_stores (
                                      user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                      store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (user_id, store_id)
);

-- 优惠券模板表
CREATE TABLE coupon_templates (
                                  id BIGSERIAL PRIMARY KEY,
                                  name VARCHAR(100) NOT NULL,       -- 优惠券名称
                                  type VARCHAR(20) NOT NULL,        -- 类型 (discount:满减, percentage:折扣, fixed:固定金额)
                                  value DECIMAL(10, 2) NOT NULL,    -- 优惠值
                                  min_amount DECIMAL(10, 2) DEFAULT 0.00, -- 最低使用金额
                                  description TEXT,                 -- 描述
                                  usage_scope VARCHAR(20) DEFAULT 'all', -- 使用范围 (all, category, product)
                                  target_ids JSONB,                 -- 适用ID (JSON数组，根据scope关联category_id或product_id)
                                  total_quantity INT DEFAULT 0,     -- 发放总数量
                                  remaining_quantity INT DEFAULT 0, -- 剩余数量
                                  validity_type VARCHAR(20) NOT NULL, -- 有效期类型 (fixed_days, fixed_date_range)
                                  valid_days INT,                   -- 有效天数 (如果validity_type是fixed_days)
                                  start_date DATE,                  -- 开始日期 (如果validity_type是fixed_date_range)
                                  end_date DATE,                    -- 结束日期 (如果validity_type是fixed_date_range)
                                  acquire_limit INT DEFAULT 1,      -- 每人领取限制
                                  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  is_active BOOLEAN DEFAULT TRUE
);

-- 用户优惠券实例表
CREATE TABLE user_coupons (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              coupon_template_id BIGINT NOT NULL REFERENCES coupon_templates(id) ON DELETE CASCADE,
                              status VARCHAR(20) DEFAULT 'available', -- 状态 (available, used, expired)
                              received_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              used_at TIMESTAMP WITH TIME ZONE,
                              order_id BIGINT, -- REFERENCES orders(id) ON DELETE SET NULL, -- 使用的订单ID - 暂时不加外键，避免循环依赖
                              expire_at TIMESTAMP WITH TIME ZONE NOT NULL, -- 过期时间 (根据模板计算)
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_coupons_user_id ON user_coupons (user_id);

-- 积分交易明细表
CREATE TABLE point_transactions (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                    points_change INT NOT NULL,       -- 积分变化 (正数获得，负数使用)
                                    balance_after_transaction INT NOT NULL, -- 交易后积分余额
                                    type VARCHAR(20) NOT NULL,        -- 类型 (earn, use)
                                    description TEXT NOT NULL,        -- 描述
                                    related_id VARCHAR(50),           -- 关联ID (订单ID, 兑换项ID)
                                    related_type VARCHAR(20),         -- 关联类型 (order, exchange_item)
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_point_transactions_user_id ON point_transactions (user_id);

-- 积分兑换项表
CREATE TABLE point_exchange_items (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(100) NOT NULL,       -- 兑换项名称
                                      points_cost INT NOT NULL,         -- 兑换所需积分
                                      target_type VARCHAR(20) NOT NULL, -- 兑换目标类型 (product, coupon_template)
                                      target_id BIGINT,                 -- 兑换目标ID (FK to Product或CouponTemplate)
                                      stock INT,                        -- 库存 (如果有限制)
                                      image_url VARCHAR(255),           -- 兑换项图片
                                      description TEXT,                 -- 描述
                                      is_active BOOLEAN DEFAULT TRUE,
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        order_no VARCHAR(50) NOT NULL UNIQUE, -- 订单编号
                        type VARCHAR(20) NOT NULL,        -- 订单类型 (delivery:配送, pickup:自取)
                        status VARCHAR(20) NOT NULL,      -- 订单状态 (created, paid, making, ready, completed, cancelled, refunded)
                        status_text VARCHAR(50),          -- 订单状态文本 (待制作)
                        total_amount DECIMAL(10, 2) NOT NULL, -- 总金额 (最终支付金额)
                        pay_amount DECIMAL(10, 2) NOT NULL,   -- 应付金额 (支付时金额)
                        product_amount DECIMAL(10, 2) NOT NULL, -- 商品总金额
                        delivery_fee DECIMAL(10, 2) DEFAULT 0.00, -- 配送费
                        package_fee DECIMAL(10, 2) DEFAULT 0.00, -- 包装费
                        discount_amount DECIMAL(10, 2) DEFAULT 0.00, -- 优惠金额 (优惠券+其他折扣)
                        points_discount_amount DECIMAL(10, 2) DEFAULT 0.00, -- 积分抵扣金额
                        balance_discount_amount DECIMAL(10, 2) DEFAULT 0.00, -- 余额抵扣金额
                        points_used INT DEFAULT 0,        -- 使用的积分
                        balance_used DECIMAL(10, 2) DEFAULT 0.00, -- 使用的余额
                        coupon_id BIGINT, -- REFERENCES user_coupons(id) ON DELETE SET NULL, -- 使用的优惠券ID - 暂时不加外键，避免循环依赖
                        delivery_address_id BIGINT REFERENCES user_addresses(id) ON DELETE SET NULL, -- 配送地址ID (如果是配送订单)
                        pickup_store_id BIGINT REFERENCES stores(id) ON DELETE SET NULL, -- 自取门店ID (如果是自取订单)
                        delivery_time_expected TIMESTAMP WITH TIME ZONE, -- 期望送达时间
                        remark TEXT,                      -- 订单备注
                        invoice_type VARCHAR(20),         -- 发票类型 (personal, company)
                        invoice_title VARCHAR(100),       -- 发票抬头
                        invoice_tax_number VARCHAR(50),   -- 纳税人识别号
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        estimated_ready_time TIMESTAMP WITH TIME ZONE, -- 预计备好时间 (自取)
                        estimated_arrival_time TIMESTAMP WITH TIME ZONE, -- 预计送达时间 (配送)
                        rider_name VARCHAR(50),           -- 骑手姓名 (配送订单)
                        rider_phone VARCHAR(20),          -- 骑手电话 (配送订单)
                        rider_longitude DECIMAL(10, 7),   -- 骑手经度 (配送订单)
                        rider_latitude DECIMAL(10, 7),    -- 骑手纬度 (配送订单)
                        pickup_code VARCHAR(20),          -- 取餐码 (自取订单)
                        pickup_time_actual TIMESTAMP WITH TIME ZONE, -- 实际取餐时间 (自取订单)
                        counter_number VARCHAR(20),       -- 取餐台号 (自取订单)
                        cancel_deadline TIMESTAMP WITH TIME ZONE, -- 取消截止时间
                        refund_deadline TIMESTAMP WITH TIME ZONE, -- 退款截止时间
                        rate_deadline TIMESTAMP WITH TIME ZONE,   -- 评价截止时间
                        is_rated BOOLEAN DEFAULT FALSE    -- 是否已评价
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);

-- 订单项表 (与订单一对多)
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT, -- 不允许删除已下单的商品
                             product_name VARCHAR(100) NOT NULL, -- 商品名称 (冗余，提高查询效率)
                             product_image_url VARCHAR(255),     -- 商品图片URL (冗余)
                             quantity INT NOT NULL,
                             price_at_order DECIMAL(10, 2) NOT NULL, -- 下单时商品价格 (价格快照)
                             original_price_at_order DECIMAL(10, 2), -- 下单时商品原价
                             subtotal DECIMAL(10, 2) NOT NULL,   -- 小计
                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

-- 订单项定制选项表 (与订单项一对多)
CREATE TABLE order_item_customizations (
                                           id BIGSERIAL PRIMARY KEY,
                                           order_item_id BIGINT NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
                                           customization_type_name VARCHAR(50) NOT NULL, -- 定制类型名称
                                           option_value VARCHAR(50) NOT NULL,            -- 选项值
                                           option_label VARCHAR(50) NOT NULL,            -- 选项显示名称
                                           price_adjustment_at_order DECIMAL(10, 2) DEFAULT 0.00, -- 下单时价格调整
                                           quantity INT DEFAULT 1,                       -- 数量 (对于加料)
                                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_item_customizations_order_item_id ON order_item_customizations (order_item_id);

-- 订单状态时间线表 (记录订单状态变更历史)
CREATE TABLE order_status_timelines (
                                        id BIGSERIAL PRIMARY KEY,
                                        order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                                        status VARCHAR(20) NOT NULL,      -- 状态 (created, paid, making, ready, completed)
                                        status_text VARCHAR(50) NOT NULL, -- 状态文本
                                        time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        is_current BOOLEAN DEFAULT FALSE, -- 标记是否为当前状态
                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_status_timelines_order_id ON order_status_timelines (order_id);

-- 支付表
CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                          pay_id VARCHAR(100),              -- 支付单ID (第三方支付系统返回)
                          transaction_id VARCHAR(100),      -- 支付平台交易号
                          pay_type VARCHAR(20) NOT NULL,    -- 支付方式 (alipay, wechat, balance)
                          channel VARCHAR(20),              -- 渠道 (miniprogram, app, h5)
                          pay_amount DECIMAL(10, 2) NOT NULL, -- 支付金额
                          pay_status VARCHAR(20) NOT NULL DEFAULT 'unpaid', -- 支付状态 (unpaid, paid, failed, cancelled)
                          pay_time TIMESTAMP WITH TIME ZONE,
                          expire_time TIMESTAMP WITH TIME ZONE, -- 支付过期时间
                          is_sandbox BOOLEAN DEFAULT FALSE, -- 是否沙箱环境
                          payment_url VARCHAR(255),         -- H5支付链接 (如果需要)
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_transaction_id ON payments (transaction_id);

-- 订单评价表
CREATE TABLE order_reviews (
                               id BIGSERIAL PRIMARY KEY,
                               order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5), -- 评分 (1-5)
                               content TEXT,                     -- 评价内容
                               is_anonymous BOOLEAN DEFAULT FALSE, -- 是否匿名
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               UNIQUE (order_id) -- 一个订单只能评价一次
);

CREATE INDEX idx_order_reviews_order_id ON order_reviews (order_id);
CREATE INDEX idx_order_reviews_user_id ON order_reviews (user_id);

-- 订单评价图片表
CREATE TABLE order_review_images (
                                     id BIGSERIAL PRIMARY KEY,
                                     review_id BIGINT NOT NULL REFERENCES order_reviews(id) ON DELETE CASCADE,
                                     image_url VARCHAR(255) NOT NULL,
                                     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_review_images_review_id ON order_review_images (review_id);

-- 评价标签主表
CREATE TABLE review_tags (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(50) NOT NULL UNIQUE
);

-- 订单评价-标签关联表 (多对多)
CREATE TABLE order_review_tags_map (
                                       review_id BIGINT NOT NULL REFERENCES order_reviews(id) ON DELETE CASCADE,
                                       tag_id BIGINT NOT NULL REFERENCES review_tags(id) ON DELETE CASCADE,
                                       PRIMARY KEY (review_id, tag_id)
);

-- 订单退款表 (如果退款流程独立且有详细记录)
CREATE TABLE order_refunds (
                               id BIGSERIAL PRIMARY KEY,
                               order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               reason VARCHAR(100) NOT NULL,
                               description TEXT,
                               status VARCHAR(20) DEFAULT 'pending', -- pending, approved, rejected, refunded
                               refund_amount DECIMAL(10, 2),         -- 实际退款金额
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_refunds_order_id ON order_refunds (order_id);

-- 订单退款图片表
CREATE TABLE order_refund_images (
                                     id BIGSERIAL PRIMARY KEY,
                                     refund_id BIGINT NOT NULL REFERENCES order_refunds(id) ON DELETE CASCADE,
                                     image_url VARCHAR(255) NOT NULL,
                                     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_refund_images_refund_id ON order_refund_images (refund_id);


-- 通知表
CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               type VARCHAR(20) NOT NULL,        -- 类型 (order, promotion, system)
                               title VARCHAR(100) NOT NULL,      -- 标题
                               content TEXT NOT NULL,            -- 内容
                               data_json JSONB,                  -- 附加数据 (JSON, 包含orderId, orderNo等)
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               is_read BOOLEAN DEFAULT FALSE,    -- 是否已读
                               read_at TIMESTAMP WITH TIME ZONE  -- 阅读时间
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_is_read ON notifications (is_read);

-- 文件表
CREATE TABLE files (
                       id BIGSERIAL PRIMARY KEY,
                       user_id BIGINT REFERENCES users(id) ON DELETE SET NULL, -- 上传用户ID
                       url VARCHAR(255) NOT NULL,        -- 文件URL
                       path VARCHAR(255) NOT NULL,       -- 文件路径
                       size INT,                         -- 文件大小 (字节)
                       type VARCHAR(20) NOT NULL,        -- 文件类型 (image, video, audio)
                       category VARCHAR(50),             -- 分类 (avatar, comment, refund)
                       width INT,                        -- 宽度 (仅图片)
                       height INT,                       -- 高度 (仅图片)
                       mime_type VARCHAR(50),            -- MIME类型
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_files_user_id ON files (user_id);
CREATE INDEX idx_files_category ON files (category);

-- 系统配置表 (键值对存储)
CREATE TABLE system_configs (
                                key VARCHAR(100) PRIMARY KEY,     -- 配置项键 (如: appName, pointsConfig.rate)
                                value TEXT,                       -- 配置项值 (可以是JSON字符串)
                                description TEXT,                 -- 描述
                                value_type VARCHAR(20) DEFAULT 'string', -- 值类型 (string, boolean, json, number)
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 页面配置表 (如果页面有独立于系统配置的特定配置)
CREATE TABLE page_configs (
                              id BIGSERIAL PRIMARY KEY,
                              page_name VARCHAR(50) NOT NULL,   -- 页面名称 (如: home)
                              key VARCHAR(100) NOT NULL,        -- 配置项键
                              value TEXT,                       -- 配置项值
                              description TEXT,
                              value_type VARCHAR(20) DEFAULT 'string',
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              UNIQUE (page_name, key)
);

-- 验证码表 (用于手机号验证、登录等)
CREATE TABLE verification_codes (
                                    id BIGSERIAL PRIMARY KEY,
                                    phone VARCHAR(20) NOT NULL,       -- 手机号
                                    code VARCHAR(10) NOT NULL,        -- 验证码
                                    type VARCHAR(20) NOT NULL,        -- 类型 (login, register, update_phone, reset_password)
                                    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                    is_used BOOLEAN DEFAULT FALSE,    -- 是否已使用
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_verification_codes_phone_type ON verification_codes (phone, type);