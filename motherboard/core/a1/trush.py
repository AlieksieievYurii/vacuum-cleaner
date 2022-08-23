if __name__ == '__main__':
    a1 = Robot()
    a1.open()
    flag = False

    # a1.beep(count=1, period=1000).expect()
    input('Press enter to start...')
    # a1.beep().expect()
    # a1.set_left_brush_motor(10).expect()
    # a1.set_main_brush_motor(20).expect()
    # a1.set_vacuum_motor(60)
    low_speed = False
    sleep(3)
    while True:
        a1.walk_forward(2000).expect()
        while True:
            if a1.input.rangefinder_center_value < 240 or \
                    a1.input.rangefinder_left_value < 240 or \
                    a1.input.rangefinder_right_value < 240:
                if not low_speed:
                    a1.walk_forward(500).expect()
                    low_speed = True
            elif low_speed:
                a1.walk_forward(2000).expect()
                low_speed = False

            if a1.input.end_left_trig:
                a1.move_backward(10, 1000).expect()
                a1.turn_right(45, 1000).expect()
                break
            elif a1.input.end_right_trig:
                a1.move_backward(10, 1000).expect()
                a1.turn_left(45, 1000).expect()
                break