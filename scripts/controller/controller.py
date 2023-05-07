def start():
    from follow import line_follow
    from rpc import greeter_server
    from webSocket.webSocket import wsapp
    from scheduler import scheduler
    from config import config
    import time
    follower = line_follow.Follower()
    greeter_server.serve()

    time.sleep(10*config.SLEEP_TIME)
    scheduler.start_continue_publish()
