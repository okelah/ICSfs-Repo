delete web_wst
where bra_code = 999
and tell_id = 1080;

update  WSTATION a set A.TELL_ID=0
where A.BRA_CODE= 999
and A.TELL_ID = 1080;

commit;

exec a07kil01;
