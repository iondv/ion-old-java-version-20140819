--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 172 (class 1259 OID 32768)
-- Name: acl; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE acl (
    actor character varying(255) NOT NULL,
    ouid character varying(255) NOT NULL,
    permissions integer
);


ALTER TABLE acl OWNER TO postgres;

--
-- TOC entry 180 (class 1259 OID 40960)
-- Name: authority_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE authority_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE authority_sequence OWNER TO postgres;

--
-- TOC entry 173 (class 1259 OID 32776)
-- Name: authority; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE authority (
    id integer DEFAULT nextval('authority_sequence'::regclass) NOT NULL,
    authority character varying(255),
    version integer
);


ALTER TABLE authority OWNER TO postgres;

--
-- TOC entry 174 (class 1259 OID 32781)
-- Name: changelog; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE changelog (
    actor character varying(255) NOT NULL,
    objectclass character varying(255) NOT NULL,
    objectid character varying(255) NOT NULL,
    "time" timestamp without time zone NOT NULL,
    type character varying(255) NOT NULL,
    attr_updates text
);


ALTER TABLE changelog OWNER TO postgres;

--
-- TOC entry 179 (class 1259 OID 32832)
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE hibernate_sequence OWNER TO postgres;

--
-- TOC entry 175 (class 1259 OID 32789)
-- Name: not_read; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE not_read (
    guid character varying(255) NOT NULL
);


ALTER TABLE not_read OWNER TO postgres;

--
-- TOC entry 176 (class 1259 OID 32794)
-- Name: user_authority; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE user_authority (
    user_id integer NOT NULL,
    authority_id integer NOT NULL
);


ALTER TABLE user_authority OWNER TO postgres;

--
-- TOC entry 177 (class 1259 OID 32801)
-- Name: user_property; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE user_property (
    name character varying(255) NOT NULL,
    value character varying(255),
    user_id integer NOT NULL
);


ALTER TABLE user_property OWNER TO postgres;

--
-- TOC entry 181 (class 1259 OID 40963)
-- Name: user_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE user_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE user_sequence OWNER TO postgres;

--
-- TOC entry 178 (class 1259 OID 32809)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE users (
    id integer DEFAULT nextval('user_sequence'::regclass) NOT NULL,
    account_expired boolean,
    account_locked boolean,
    enabled boolean,
    password character varying(255),
    password_expired boolean,
    username character varying(255),
    version integer
);


ALTER TABLE users OWNER TO postgres;

--
-- TOC entry 2043 (class 0 OID 32768)
-- Dependencies: 172
-- Data for Name: acl; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 2044 (class 0 OID 32776)
-- Dependencies: 173
-- Data for Name: authority; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 2061 (class 0 OID 0)
-- Dependencies: 180
-- Name: authority_sequence; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('authority_sequence', 1, false);


--
-- TOC entry 2045 (class 0 OID 32781)
-- Dependencies: 174
-- Data for Name: changelog; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 2062 (class 0 OID 0)
-- Dependencies: 179
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('hibernate_sequence', 1, false);


--
-- TOC entry 2046 (class 0 OID 32789)
-- Dependencies: 175
-- Data for Name: not_read; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 2047 (class 0 OID 32794)
-- Dependencies: 176
-- Data for Name: user_authority; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 2048 (class 0 OID 32801)
-- Dependencies: 177
-- Data for Name: user_property; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 2063 (class 0 OID 0)
-- Dependencies: 181
-- Name: user_sequence; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('user_sequence', 2, true);


--
-- TOC entry 2049 (class 0 OID 32809)
-- Dependencies: 178
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO users (id, account_expired, account_locked, enabled, password, password_expired, username, version) VALUES (2, NULL, NULL, true, '$2a$10$MU83UwFO79OgGzqo8/EbA.GzpcZlBvzVT.uPF9isKAOVH5cEM/Eui', NULL, 'admin', NULL);

INSERT INTO user_authority (user_id, authority_id) VALUES (1,1);
INSERT INTO user_authority (user_id, authority_id) VALUES (2,1);

INSERT INTO authority (id, version, authority) VALUES (1,0,'ROLE_USER');
INSERT INTO authority (id, version, authority) VALUES (2,0,'ROLE_ADMIN');

INSERT INTO acl (actor,ouid, permissions) VALUES ('ROLE_USER','c:::SmevEntity',1);
--
-- TOC entry 1916 (class 2606 OID 32775)
-- Name: acl_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY acl
    ADD CONSTRAINT acl_pkey PRIMARY KEY (actor, ouid);


--
-- TOC entry 1918 (class 2606 OID 32780)
-- Name: authority_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY authority
    ADD CONSTRAINT authority_pkey PRIMARY KEY (id);


--
-- TOC entry 1920 (class 2606 OID 32788)
-- Name: changelog_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY changelog
    ADD CONSTRAINT changelog_pkey PRIMARY KEY (actor, objectclass, objectid, "time", type);


--
-- TOC entry 1922 (class 2606 OID 32793)
-- Name: not_read_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY not_read
    ADD CONSTRAINT not_read_pkey PRIMARY KEY (guid);


--
-- TOC entry 1924 (class 2606 OID 32800)
-- Name: user_authority_authority_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY user_authority
    ADD CONSTRAINT user_authority_authority_id_key UNIQUE (authority_id);


--
-- TOC entry 1926 (class 2606 OID 32798)
-- Name: user_authority_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY user_authority
    ADD CONSTRAINT user_authority_pkey PRIMARY KEY (user_id, authority_id);


--
-- TOC entry 1928 (class 2606 OID 32808)
-- Name: user_property_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY user_property
    ADD CONSTRAINT user_property_pkey PRIMARY KEY (name, user_id);


--
-- TOC entry 1930 (class 2606 OID 32816)
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 1931 (class 2606 OID 32817)
-- Name: fkb55bebcf8daf0609; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY user_authority
    ADD CONSTRAINT fkb55bebcf8daf0609 FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 1932 (class 2606 OID 32822)
-- Name: fkb55bebcfbc20a00b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY user_authority
    ADD CONSTRAINT fkb55bebcfbc20a00b FOREIGN KEY (authority_id) REFERENCES authority(id);


--
-- TOC entry 1933 (class 2606 OID 32827)
-- Name: fkc7d137c98daf0609; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY user_property
    ADD CONSTRAINT fkc7d137c98daf0609 FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2059 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2015-04-08 16:29:32

--
-- PostgreSQL database dump complete
--

