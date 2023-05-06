package cn.biq.mn.user.user;


public class UserMapper {

    public static UserSessionVo toSessionVo(User user) {
        if (user == null) return null;
        UserSessionVo userSessionVo = new UserSessionVo();
        userSessionVo.setId( user.getId() );
        userSessionVo.setUsername(user.getUsername());
        userSessionVo.setNickName(user.getNickName());
        userSessionVo.setHeadimgurl(user.getHeadimgurl());
        return userSessionVo;
    }

}
