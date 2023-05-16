def start():
    from follow import line_follow
    from scheduler import scheduler
    from odometry import odometry
    from config import config
    from info.info_manager import info_manager
    from helper.helper import helper
    import time
    import signal
    follower = line_follow.Follower()
    odom = odometry.odometry()
    sche = scheduler.scheduler()

    def __del__():
        sche.__del__()
        helper.__del__()

    time.sleep(10*config.SLEEP_TIME)
    sche.start_all()
    signal.signal(signal.SIGINT, lambda signum, frame: __del__())

    # test
    info_manager.set_linear_x(0.05)
