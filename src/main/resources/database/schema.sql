create database inventory_management;

use inventory_management;

create table categories(
    category_id varchar(20) primary key,
    name varchar(100) unique not null,
    description varchar(500)
);

create table suppliers(
    supplier_id varchar(20) primary key,
    name varchar(100) not null,
    email varchar(255) unique not null,
    phone varchar(20)
);

create table products(
    product_id varchar(20) primary key,
    sku varchar(20) not null unique,
    name varchar(100) not null,
    category_id varchar(20) not null,
    foreign key(category_id) references categories(category_id),
    price Decimal(10,2) not null check ( price > 0 ),
    quantity int not null check ( quantity >= 0 ),
    reorder_level int not null check ( reorder_level >= 0 )
);

create table product_suppliers(
    primary key(product_id, supplier_id),
    product_id varchar(20) not null,
    foreign key(product_id) references products(product_id) on delete cascade,
    supplier_id varchar(20) not null,
    foreign key(supplier_id) references suppliers(supplier_id)
);

create table stock_movements(
    movement_id varchar(20) primary key,
    product_id varchar(20) not null,
    foreign key(product_id) references products(product_id),
    type varchar(20) not null,
    quantity int not null check ( quantity > 0 ),
    timestamp datetime not null
);


