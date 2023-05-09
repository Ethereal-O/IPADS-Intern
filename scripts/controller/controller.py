def start():
    from follow import line_follow
    from rpc import greeter_server
    from webSocket import webSocket
    from scheduler import scheduler
    from odometry import odometry
    from config import config
    from info.info_manager import info_manager
    import time
    follower = line_follow.Follower()
    odom = odometry.odometry()
    # wsapp = webSocket.webSocket()
    sche = scheduler.scheduler()
    greeter_server.serve()

    time.sleep(10*config.SLEEP_TIME)
    sche.start_continue_publish()
    
    # test
    info_manager.set_linear_x(0.18)
    
