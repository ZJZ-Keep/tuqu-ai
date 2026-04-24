package org.example.zjzaiagent.agent;
/**
 ReAct (Reasoning and Acting) 模式的代理抽象类
 实现了思考-行动的循环模式
 */
public abstract class ReActAgent extends BaseAgent{

    /**
     * 思考
     */
    public abstract boolean think();

    /**
     * 执行
     * @return
     */
    public abstract String act();


    /**
     * 执行单个步骤
     * @return
     */
    @Override
    public String step() {
        try {
            if (this.think()) {
                return this.act();
            } else {
                return "思考完成-无需行动";
            }
        } catch (Exception e) {
            return "步骤执行失败：" + e.getMessage();
        }
    }
}
