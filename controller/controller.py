def start():
    from follow import line_follow
    from scheduler import scheduler
    from odometry import odometry
    from config import config
    from info.info_manager import info_manager_thread_local
    from helper.helper import helper
    import time
    follower = line_follow.Follower()
    odom = odometry.odometry()
    sche = scheduler.scheduler()

    def __del__():
        helper.__del__()
        sche.__del__()

    time.sleep(10*config.SLEEP_TIME)
    sche.start_all()

    # test
    if info_manager_thread_local.info_manager.get_id() == 0:
        info_manager_thread_local.info_manager.set_linear_x(0.05)
    # info_manager_thread_local.info_manager.set_linear_x(0.05)
    return __del__
