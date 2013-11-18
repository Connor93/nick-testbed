Drop table if exists "ContentEntity";

Create table if not exists "ContentEntity" (
	"_id" UUID primary key,
	"typeCode" INTEGER not null,
	"urlKey" TEXT not null unique,
	"title" TEXT not null,
	"shortTitle" TEXT not null,
	"datePosted" TEXT not null,
	"dateFetched" TEXT not null,
	"json" BLOB not null,
	"jsonComplete" INTEGER not null 
);

create index "urlKey_idx" on "ContentEntity" ("urlKey" asc);

Drop table if exists "ContentLink";

Create table if not exists "ContentLink" (
	"typeCode" INTEGER not null,
	"parent" UUID not null REFERENCES "ContentEntity"("_id") on delete cascade,
	"child" UUID not null REFERENCES "ContentEntity"("_id") on delete cascade,
	"override" UUID REFERENCES "ContentEntity"("_id") on delete cascade,
	"order" INTEGER not null
);

create unique index "unique_link_idx" on "ContentLink" ("parent", "child", "typeCode");

Drop table if exists "Asset";

Create table "Asset" (
	url TEXT primary key, 
	contentId UUID not null REFERENCES "ContentEntity" ("_id") on delete cascade, 
	typeCode INTEGER not null, 
	aspectRatio INTEGER not null, 
	width INTEGER not null, 
	height INTEGER not null);
